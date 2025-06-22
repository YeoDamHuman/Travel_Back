package com.example.backend.schedule.service;

import com.example.backend.group.entity.Group;
import com.example.backend.group.repository.GroupRepository;
import com.example.backend.schedule.dto.request.ScheduleRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.repository.ScheduleRepository;
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

    // 1️⃣ 스케쥴 만들기
    @Transactional
    public UUID createSchedule(ScheduleRequest.scheduleCreateRequest request) {
        // ✅ JWT 인증 검증
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        // ✅ 현재 로그인한 사용자 ID 추출
        String currentUserIdString = authentication.getName();
        UUID currentUserId;
        try {
            currentUserId = UUID.fromString(currentUserIdString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID 형식입니다.", e);
        }

        // ✅ 그룹과 멤버 정보 함께 조회 (users 멤버 리스트 포함)
        Group existingGroup = groupRepository.findByIdWithUsers(request.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // ✅ 현재 사용자가 그룹 멤버인지 확인
        boolean isMember = existingGroup.getUsers().stream()
                .anyMatch(user -> user.getUserId().equals(currentUserId));

        if (!isMember) {
            throw new IllegalStateException("해당 그룹의 멤버가 아닙니다.");
        }

        // ✅ 스케줄 생성 및 저장
        Schedule schedule = Schedule.builder()
                .scheduleName(request.getScheduleName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .groupId(existingGroup)
                .build();

        Schedule saveSchedule = scheduleRepository.save(schedule);

        return saveSchedule.getScheduleId();
    }

    @Transactional
    public List<ScheduleResponse.scheduleInfo> getScheduleByGroupId(UUID groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }

        String currentUserIdString = authentication.getName();
        UUID currentUserId;
        try {
            currentUserId = UUID.fromString(currentUserIdString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID 형식입니다.", e);
        }

        // 2. 그룹 조회 및 멤버 여부 확인
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        boolean isMember = group.getUsers().stream()
                .anyMatch(user -> user.getUserId().equals(currentUserId));

        if (!isMember) {
            throw new IllegalStateException("해당 그룹의 멤버가 아닙니다.");
        }

        List<Schedule> schedules = scheduleRepository.findAllByGroupId(groupId);

        return schedules.stream()
                .map(schedule -> ScheduleResponse.scheduleInfo.builder()
                        .scheduleId(schedule.getScheduleId())
                        .scheduleName(schedule.getScheduleName())
                        .startDate(schedule.getStartDate())
                        .endDate(schedule.getEndDate())
                        .createdAt(schedule.getCreatedAt())
                        .updatedAt(schedule.getUpdatedAt())
                        .budget(schedule.getBudget())
                        .groupId(schedule.getGroupId().getGroupId())
                        .groupName(schedule.getGroupId().getGroupName())
                        .build())
                .collect(Collectors.toList());
    }
}
