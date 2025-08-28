package com.example.backend.schedule.filter;

import com.example.backend.common.auth.AuthUtil;
import com.example.backend.group.entity.Group;
import com.example.backend.group.service.GroupService;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.entity.ScheduleType;
import com.example.backend.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 스케줄 및 그룹에 대한 접근 권한을 검증하는 필터 클래스입니다.
 * 이 클래스는 스케줄 생성, 조회, 수정 등의 요청에 대해 현재 사용자가
 * 적절한 권한을 가지고 있는지 확인하는 역할을 담당합니다.
 */
@RequiredArgsConstructor
@Component
public class ScheduleFilter {

    private final GroupService groupService;
    private final ScheduleRepository scheduleRepository;

    /**
     * 스케줄 생성 요청의 유효성을 검증합니다.
     * <p>
     * 스케줄 타입이 '그룹'일 경우, groupId가 필수적으로 필요하며,
     * 현재 사용자가 해당 그룹의 멤버인지 확인합니다. '개인' 스케줄일 경우,
     * groupId가 null인지 확인합니다.
     *
     * @param request 스케줄 타입 (개인 또는 그룹).
     * @param groupId 그룹 ID. 개인 스케줄일 경우 null이어야 합니다.
     * @return 유효한 그룹 스케줄 요청인 경우 {@link Group} 엔티티를 반환하고,
     * 개인 스케줄인 경우 null을 반환합니다.
     * @throws IllegalArgumentException 스케줄 타입에 맞지 않게 groupId가 제공된 경우 발생합니다.
     * @throws IllegalStateException    현재 사용자가 해당 그룹의 멤버가 아닌 경우 발생합니다.
     */
    public Group validateScheduleRequest(ScheduleType request, UUID groupId) {
        UUID userId = AuthUtil.getCurrentUserId();
        if (request == ScheduleType.GROUP) {
            if (groupId == null) {
                throw new IllegalArgumentException("그룹 스케줄 생성 시 groupId는 필수입니다.");
            }
            Group group = groupService.findByIdWithUsers(groupId);
            boolean isMember = group.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(userId));
            if (!isMember) {
                throw new IllegalStateException("해당 그룹의 멤버가 아닙니다. 그룹 스케줄을 생성할 수 없습니다.");
            }
            return group;
        }
        if (request == ScheduleType.PERSONAL && groupId != null) {
            throw new IllegalArgumentException("개인 스케줄 생성 시 groupId는 null이어야 합니다.");
        }
        return null;
    }

    /**
     * 특정 스케줄에 대한 사용자의 접근 권한을 검증합니다.
     * <p>
     * 스케줄 타입이 '개인'인 경우, 해당 스케줄의 소유자인지 확인합니다.
     * '그룹'인 경우, 해당 스케줄이 속한 그룹의 멤버인지 확인합니다.
     *
     * @param scheduleId    접근을 검증할 스케줄의 ID.
     * @param currentUserId 현재 로그인된 사용자의 ID.
     * @throws IllegalArgumentException 스케줄을 찾을 수 없거나 유효하지 않은 스케줄 타입인 경우 발생합니다.
     * @throws IllegalStateException    개인 스케줄의 소유자가 아니거나 그룹의 멤버가 아닌 경우 발생합니다.
     */
    public void validateScheduleAccess(UUID scheduleId, UUID currentUserId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));
        if (schedule.getScheduleType() == ScheduleType.PERSONAL) {
            if (!schedule.getUserId().getUserId().equals(currentUserId)) {
                throw new IllegalStateException("개인 스케줄에 대한 권한이 없습니다.");
            }
        } else if (schedule.getScheduleType() == ScheduleType.GROUP) {
            Group groupEntity = groupService.findByIdWithUsers(schedule.getGroupId().getGroupId());
            boolean isMember = groupEntity.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("그룹 멤버가 아니어서 스케줄에 대한 권한이 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 스케줄 타입입니다.");
        }
    }

    /**
     * 특정 그룹에 대한 사용자의 접근 권한을 검증하고 그룹 엔티티를 반환합니다.
     * <p>
     * 이 메서드는 현재 사용자가 주어진 그룹 ID에 해당하는 그룹의 멤버인지 확인합니다.
     * 주로 그룹 스케줄을 조회할 때 사용됩니다.
     *
     * @param groupId       접근을 검증할 그룹의 ID.
     * @param currentUserId 현재 로그인된 사용자의 ID.
     * @return 사용자가 멤버인 유효한 {@link Group} 엔티티를 반환합니다.
     * @throws IllegalStateException 현재 사용자가 해당 그룹의 멤버가 아닌 경우 발생합니다.
     */
    public Group validateGroupAccess(UUID groupId, UUID currentUserId) {
        Group group = groupService.findByIdWithUsers(groupId);
        boolean isMember = group.getUsers().stream()
                .anyMatch(user -> user.getUserId().equals(currentUserId));
        if (!isMember) {
            throw new IllegalStateException("해당 그룹의 멤버가 아닙니다. 그룹 스케줄을 조회할 수 없습니다.");
        }
        return group;
    }
}