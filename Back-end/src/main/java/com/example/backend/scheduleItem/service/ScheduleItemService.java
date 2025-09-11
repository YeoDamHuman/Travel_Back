package com.example.backend.scheduleItem.service;

import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.repository.ScheduleRepository;
import com.example.backend.scheduleItem.dto.request.ScheduleItemRequest.ScheduleItemCreateRequest;
import com.example.backend.scheduleItem.dto.request.ScheduleItemRequest.ScheduleItemUpdateRequest;
import com.example.backend.scheduleItem.entity.ScheduleItem;
import com.example.backend.scheduleItem.repository.ScheduleItemRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.example.backend.scheduleItem.dto.request.ScheduleItemRequest.ScheduleItemCreateRequest.toEntity;

/**
 * 스케쥴 아이템 관련 비즈니스 로직을 처리하는 서비스 클래스.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduleItemService {
    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleRepository scheduleRepository;

    /**
     * 스케쥴 아이템을 단일 생성합니다.
     *
     * @param scheduleId 아이템을 추가할 스케쥴의 ID
     * @param request    생성할 스케쥴 아이템 정보를 담은 DTO
     */
    @Transactional
    public void itemCreate(UUID scheduleId, ScheduleItemCreateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케줄을 찾을 수 없습니다."));

        ScheduleItem scheduleItem = toEntity(request, schedule);
        scheduleItemRepository.save(scheduleItem);
    }

    /**
     * 스케쥴 아이템 리스트를 생성합니다.
     *
     * @param scheduleItems 생성할 스케쥴 아이템 엔티티 리스트
     */
    @Transactional
    public void createItemList(List<ScheduleItem> scheduleItems) {
        scheduleItemRepository.saveAll(scheduleItems);
    }

    /**
     * 스케쥴 아이템을 수정합니다.
     *
     * @param request 수정할 스케쥴 아이템 정보를 담은 DTO
     * @return 수정된 스케쥴 아이템의 UUID
     */
    @Transactional
    public UUID itemUpdate(ScheduleItemUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new EntityNotFoundException("해당 스케줄을 찾을 수 없습니다."));

        ScheduleItem scheduleItem = ScheduleItemUpdateRequest.toEntity(request, schedule);

        ScheduleItem item = scheduleItemRepository.save(scheduleItem);
        return item.getScheduleItemId();
    }

    /**
     * 스케쥴 아이템을 삭제합니다.
     *
     * @param scheduleId     삭제할 스케쥴의 ID
     * @param scheduleItemId 삭제할 스케쥴 아이템의 UUID
     * @throws EntityNotFoundException 주어진 ID에 해당하는 스케쥴 또는 아이템을 찾을 수 없을 경우
     */
    @Transactional
    public void itemDelete(UUID scheduleId, UUID scheduleItemId) {
        // 1. 스케줄 존재 확인
        scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케쥴을 찾을 수 없습니다."));

        // 2. 아이템 존재 확인
        ScheduleItem item = scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케쥴 아이템을 찾을 수 없습니다."));

        // 3. 아이템이 해당 스케줄에 속해 있는지 체크
        if (!item.getScheduleId().getScheduleId().equals(scheduleId)) {
            throw new IllegalArgumentException("이 스케쥴에 속하지 않는 아이템입니다.");
        }

        // 4. 삭제
        scheduleItemRepository.delete(item);
    }
}