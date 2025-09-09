package com.example.backend.scheduleItem.service;

import com.example.backend.common.auth.AuthUtil;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.repository.ScheduleRepository;
import com.example.backend.scheduleItem.dto.request.ScheduleItemRequest.ScheduleItemCreateRequest;
import com.example.backend.scheduleItem.dto.request.ScheduleItemRequest.ScheduleItemUpdateRequest;
import com.example.backend.scheduleItem.entity.ScheduleItem;
import com.example.backend.scheduleItem.repository.ScheduleItemRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 스케쥴 아이템 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class ScheduleItemService {
    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    /**
     * 특정 스케줄에 새로운 아이템을 생성합니다.
     */
    @Transactional
    public void itemCreate(UUID scheduleId, ScheduleItemCreateRequest request) {
        User currentUser = AuthUtil.getCurrentUser(userRepository);
        Schedule schedule = scheduleRepository.findWithUsersById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));

        if (schedule.getUsers().stream().noneMatch(user -> user.equals(currentUser))) {
            throw new AccessDeniedException("스케줄에 아이템을 추가할 권한이 없습니다.");
        }

        ScheduleItem scheduleItem = ScheduleItemCreateRequest.toEntity(request, schedule);
        scheduleItemRepository.save(scheduleItem);
    }

    /**
     * 스케줄 아이템 리스트를 생성합니다. (내부용)
     */
    @Transactional
    public void createItemList(List<ScheduleItem> scheduleItems) {
        scheduleItemRepository.saveAll(scheduleItems);
    }

    /**
     * 스케줄 아이템을 수정합니다.
     */
    @Transactional
    public UUID itemUpdate(ScheduleItemUpdateRequest request) {
        User currentUser = AuthUtil.getCurrentUser(userRepository);
        ScheduleItem originalItem = scheduleItemRepository.findById(request.getScheduleItemId())
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄 아이템을 찾을 수 없습니다."));

        Schedule schedule = originalItem.getScheduleId();
        if (schedule.getUsers().stream().noneMatch(user -> user.equals(currentUser))) {
            throw new AccessDeniedException("스케줄 아이템을 수정할 권한이 없습니다.");
        }

        ScheduleItem updatedItem = ScheduleItemUpdateRequest.toEntity(request, originalItem);
        scheduleItemRepository.save(updatedItem);

        return updatedItem.getScheduleItemId();
    }

    /**
     * 스케줄 아이템을 삭제합니다.
     */
    @Transactional
    public void itemDelete(UUID scheduleItemId) {
        User currentUser = AuthUtil.getCurrentUser(userRepository);
        ScheduleItem scheduleItem = scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄 아이템을 찾을 수 없습니다."));

        Schedule schedule = scheduleItem.getScheduleId();
        if (schedule.getUsers().stream().noneMatch(user -> user.equals(currentUser))) {
            throw new AccessDeniedException("스케줄 아이템을 삭제할 권한이 없습니다.");
        }

        scheduleItemRepository.delete(scheduleItem);
    }
}