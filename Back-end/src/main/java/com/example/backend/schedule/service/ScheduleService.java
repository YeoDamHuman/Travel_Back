package com.example.backend.schedule.service;

import com.example.backend.group.entity.Group;
import com.example.backend.group.repository.GroupRepository;
import com.example.backend.schedule.dto.request.ScheduleRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.entity.ScheduleType;
import com.example.backend.schedule.repository.ScheduleRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // ✅ UserId 구하기
    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return UUID.fromString(authentication.getName());
    }

    // 1️⃣ 스케쥴 생성
    @Transactional
    public UUID createSchedule(ScheduleRequest.scheduleCreateRequest request) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        Group groupEntity = null;
        if (request.getScheduleType() == ScheduleType.GROUP) {
            if (request.getGroupId() == null) {
                throw new IllegalArgumentException("그룹 스케줄 생성 시 groupId는 필수입니다.");
            }
            groupEntity = groupRepository.findByIdWithUsers(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

            boolean isMember = groupEntity.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("해당 그룹의 멤버가 아닙니다. 그룹 스케줄을 생성할 수 없습니다.");
            }
        } else if (request.getScheduleType() == ScheduleType.PERSONAL) {
            if (request.getGroupId() != null) {
                throw new IllegalArgumentException("개인 스케줄 생성 시 groupId는 null이어야 합니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 스케줄 타입입니다.");
        }

        Schedule schedule = Schedule.builder()
                .scheduleName(request.getScheduleName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .groupId(groupEntity)
                .userId(currentUser)
                .scheduleType(request.getScheduleType())
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        return savedSchedule.getScheduleId();
    }

    // 2️⃣ 스케쥴 수정
    @Transactional
    public UUID updateSchedule(ScheduleRequest.scheduleUpdateRequest request) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 스케줄을 찾을 수 없습니다."));

        if (schedule.getScheduleType() == ScheduleType.PERSONAL) {
            if (!schedule.getUserId().getUserId().equals(currentUserId)) {
                throw new IllegalStateException("개인 스케줄에 대한 수정 권한이 없습니다.");
            }
        } else if (schedule.getScheduleType() == ScheduleType.GROUP) {
            Group groupEntity = groupRepository.findByIdWithUsers(schedule.getGroupId().getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 스케줄의 그룹을 찾을 수 없습니다."));

            boolean isMember = groupEntity.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("그룹 멤버가 아니어서 스케줄을 수정할 수 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 스케줄 타입입니다.");
        }

        Group groupEntity = null;
        if (request.getScheduleType() == ScheduleType.GROUP) {
            if (request.getGroupId() == null) {
                throw new IllegalArgumentException("그룹 스케줄 수정 시 groupId는 필수입니다.");
            }
            groupEntity = groupRepository.findByIdWithUsers(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

            boolean isMember = groupEntity.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("해당 그룹의 멤버가 아닙니다.");
            }
        } else if (request.getScheduleType() == ScheduleType.PERSONAL) {
            if (request.getGroupId() != null) {
                throw new IllegalArgumentException("개인 스케줄 수정 시 groupId는 null이어야 합니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 스케줄 타입입니다.");
        }

        schedule.updateSchedule(
                request.getScheduleName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getBudget(),
                groupEntity,
                request.getScheduleType()
        );

        scheduleRepository.save(schedule);
        return schedule.getScheduleId();
    }

    // 3️⃣ 스케쥴 삭제
    @Transactional
    public void deleteSchedule(UUID scheduleId) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("삭제하려는 스케줄을 찾을 수 없습니다."));

        if (schedule.getScheduleType() == ScheduleType.PERSONAL) {
            if (!schedule.getUserId().getUserId().equals(currentUserId)) {
                throw new IllegalStateException("개인 스케줄에 대한 삭제 권한이 없습니다.");
            }
        } else if (schedule.getScheduleType() == ScheduleType.GROUP) {
            Group group = groupRepository.findByIdWithUsers(schedule.getGroupId().getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 스케줄의 그룹을 찾을 수 없습니다."));

            boolean isMember = group.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("그룹 멤버가 아니어서 스케줄을 삭제할 수 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 스케줄 타입입니다.");
        }

        scheduleRepository.delete(schedule);
    }


    // 4️⃣ 스케쥴 조회
    @Transactional
    public List<ScheduleResponse.scheduleInfo> getSchedules(UUID groupId) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        List<Schedule> schedules;

        if (groupId != null) {
            Group groupEntity = groupRepository.findByIdWithUsers(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

            boolean isMember = groupEntity.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("해당 그룹의 멤버가 아닙니다. 그룹 스케줄을 조회할 수 없습니다.");
            }
            schedules = scheduleRepository.findAllByGroupId(groupEntity);
            schedules = schedules.stream()
                    .filter(s -> s.getScheduleType() == ScheduleType.GROUP)
                    .collect(Collectors.toList());
        } else {
            schedules = scheduleRepository.findAllByUserIdAndScheduleType(currentUser, ScheduleType.PERSONAL);
        }

        return schedules.stream()
                .map(schedule -> {
                    UUID responseGroupId = (schedule.getScheduleType() == ScheduleType.GROUP && schedule.getGroupId() != null)
                            ? schedule.getGroupId().getGroupId() : null;
                    String responseGroupName = (schedule.getScheduleType() == ScheduleType.GROUP && schedule.getGroupId() != null)
                            ? schedule.getGroupId().getGroupName() : null;

                    return ScheduleResponse.scheduleInfo.builder()
                            .scheduleId(schedule.getScheduleId())
                            .scheduleName(schedule.getScheduleName())
                            .startDate(schedule.getStartDate())
                            .endDate(schedule.getEndDate())
                            .createdAt(schedule.getCreatedAt())
                            .updatedAt(schedule.getUpdatedAt())
                            .budget(schedule.getBudget())
                            .groupId(responseGroupId)
                            .groupName(responseGroupName)
                            .userId(schedule.getUserId().getUserId())
                            .scheduleType(schedule.getScheduleType().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // 5️⃣ 스케쥴 상세 조회
    @Transactional
    public ScheduleResponse.scheduleDetailResponse getScheduleDetail(UUID scheduleId) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));

        if (schedule.getScheduleType() == ScheduleType.PERSONAL) {
            if (!schedule.getUserId().getUserId().equals(currentUserId)) {
                throw new IllegalStateException("개인 스케줄에 접근할 수 없습니다.");
            }
        } else if (schedule.getScheduleType() == ScheduleType.GROUP) {
            Group group = groupRepository.findByIdWithUsers(schedule.getGroupId().getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 스케줄의 그룹을 찾을 수 없습니다."));
            boolean isMember = group.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("해당 그룹의 멤버가 아닙니다. 스케줄을 조회할 수 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 스케쥴 타입입니다.");
        }

        return ScheduleResponse.scheduleDetailResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .budget(schedule.getBudget())
                .build();
    }
}
