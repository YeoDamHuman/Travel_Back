package com.example.backend.scheduleItem.service;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.service.ScheduleService;
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
@RequiredArgsConstructor
public class ScheduleItemService {
    private final ScheduleItemRepository scheduleItemRepository;

    /**
     * 스케쥴 아이템을 단일 생성합니다.
     *
     * @param schedule 생성할 스케쥴 엔티티
     * @param request 생성할 스케쥴 아이템 정보를 담은 DTO
     */
    @Transactional
    public void itemCreate(Schedule schedule, ScheduleItemCreateRequest request) {
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
        ScheduleItem scheduleItem = ScheduleItemUpdateRequest.toEntity(request);
        ScheduleItem item = scheduleItemRepository.save(scheduleItem);
        return item.getScheduleItemId();
    }

    /**
     * 스케쥴 아이템을 삭제합니다.
     *
     * @param scheduleItemId 삭제할 스케쥴 아이템의 UUID
     * @throws EntityNotFoundException 주어진 ID에 해당하는 스케쥴 아이템을 찾을 수 없을 경우
     */
    @Transactional
    public void itemDelete(UUID scheduleItemId) {
        scheduleItemRepository.findById(scheduleItemId)
                .orElseThrow(() -> new EntityNotFoundException("해당 스케쥴 아이템을 찾을 수 없습니다."));
        scheduleItemRepository.deleteById(scheduleItemId);
    }
}