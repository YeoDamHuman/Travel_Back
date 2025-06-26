package com.example.backend.scheduleItem.controller;

import com.example.backend.scheduleItem.dto.request.ScheduleItemRequest;
import com.example.backend.scheduleItem.dto.response.ScheduleItemResponse;
import com.example.backend.scheduleItem.service.ScheduleItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/schedule/items")
@RequiredArgsConstructor
@Tag(name = "ScheduleItemAPI", description = "스케쥴 아이템 관련 API")
public class ScheduleItemController {

    private final ScheduleItemService scheduleItemService;

    @PostMapping("/create/{scheduleId}")
    @Operation(summary = "스케쥴 아이템 생성", description = "특정 스케쥴에 아이템을 생성하는 API")
    public ResponseEntity<ScheduleItemResponse.scheduleItemCreateResponse> createItem(
            @Parameter(description = "스케쥴 ID", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable UUID scheduleId,
            @RequestBody ScheduleItemRequest.scheduleItemCreateRequest item) {

        ScheduleItemResponse.scheduleItemCreateResponse response = scheduleItemService.itemCreate(scheduleId, item);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update")
    @Operation(summary = "스케쥴 아이템 수정", description = "특정 스케쥴 아이템을 수정하는 API")
    public ResponseEntity<ScheduleItemResponse.scheduleItemUpdateResponse> updateItem(
            @RequestBody ScheduleItemRequest.scheduleItemUpdateRequest item) {
        ScheduleItemResponse.scheduleItemUpdateResponse response = scheduleItemService.itemUpdate(item);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{scheduleItemId}")
    @Operation(summary = "스케쥴 아이템 삭제", description = "특정 스케쥴 아이템을 삭제하는 API")
    public ResponseEntity<ScheduleItemResponse.scheduleItemDeleteResponse> deleteItem(
            @Parameter(description = "스케쥴 아이템 ID", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable UUID scheduleItemId) {

        scheduleItemService.itemDelete(scheduleItemId);

        return ResponseEntity.ok(
                ScheduleItemResponse.scheduleItemDeleteResponse.builder()
                        .message("스케쥴 아이템이 성공적으로 삭제되었습니다.")
                        .build()
        );
    }
}
