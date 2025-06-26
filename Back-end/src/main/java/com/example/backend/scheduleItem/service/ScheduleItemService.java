package com.example.backend.scheduleItem.service;

import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.repository.ScheduleRepository;
import com.example.backend.scheduleItem.dto.request.ScheduleItemRequest;
import com.example.backend.scheduleItem.dto.response.ScheduleItemResponse;
import com.example.backend.scheduleItem.entity.ScheduleItem;
import com.example.backend.scheduleItem.repository.ScheduleItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleItemService {
    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleRepository scheduleRepository;

    // 1️⃣ 스케쥴 아이템 생성
    @Transactional
    public ScheduleItemResponse.scheduleItemCreateResponse itemCreate(UUID scheduleId, ScheduleItemRequest.scheduleItemCreateRequest item) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케쥴을 찾을 수 없습니다."));

        ScheduleItem scheduleItem = ScheduleItem.builder()
                .placeId(item.getPlaceId())
                .dayNumber(item.getDayNumber())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .memo(item.getMemo())
                .cost(item.getCost())
                .scheduleId(schedule)
                .build();
        scheduleItemRepository.save(scheduleItem);
        return ScheduleItemResponse.scheduleItemCreateResponse.builder()
                .message("스케쥴 아이템 생성 성공")
                .placeId(scheduleItem.getPlaceId())
                .build();
    }

    // 2️⃣ 스케쥴 아이템 수정
    @Transactional
    public ScheduleItemResponse.scheduleItemUpdateResponse itemUpdate(ScheduleItemRequest.scheduleItemUpdateRequest request) {
        ScheduleItem scheduleItem = scheduleItemRepository.findById(request.getScheduleItemId())
                .orElseThrow(() -> new IllegalArgumentException("해당 스케쥴 아이템을 찾을 수 없습니다."));
        scheduleItem.updateScheduleItem(
                request.getPlaceId(),
                request.getDayNumber(),
                request.getStartTime(),
                request.getEndTime(),
                request.getMemo(),
                request.getCost()
        );
        scheduleItemRepository.save(scheduleItem);
        return ScheduleItemResponse.scheduleItemUpdateResponse.builder()
                .message("스케쥴 아이템이 성공적으로 업데이트되었습니다.")
                .scheduleItemId(scheduleItem.getScheduleItemId())
                .build();
    }

    // 3️⃣ 스케쥴 아이템 삭제
    @Transactional
    public void itemDelete(UUID scheduleItemId) {
        if (!scheduleItemRepository.existsById(scheduleItemId)) {
            throw new IllegalArgumentException("해당 스케쥴 아이템을 찾을 수 없습니다.");
        }
        scheduleItemRepository.deleteById(scheduleItemId);
    }


}
