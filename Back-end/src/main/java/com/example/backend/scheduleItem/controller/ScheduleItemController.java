package com.example.backend.scheduleItem.controller;

import com.example.backend.scheduleItem.dto.request.ScheduleItemRequest.ScheduleItemCreateRequest;
import com.example.backend.scheduleItem.dto.request.ScheduleItemRequest.ScheduleItemUpdateRequest;
import com.example.backend.scheduleItem.service.ScheduleItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


/**
 * 스케쥴 아이템 관련 API 요청을 처리하는 컨트롤러 클래스.
 */
@RestController
@RequestMapping("/schedule/items")
@RequiredArgsConstructor
@Tag(name = "ScheduleItemAPI", description = "스케쥴 아이템 관련 API")
public class ScheduleItemController {

    private final ScheduleItemService scheduleItemService;

    /**
     * 특정 스케쥴에 새로운 아이템을 생성합니다.
     *
     * @param scheduleId 아이템을 생성할 스케쥴의 고유 ID
     * @param request 생성할 스케쥴 아이템의 상세 정보를 담은 DTO
     * @return 성공 시 HTTP 201 Created 응답
     */
    @PostMapping("/{scheduleId}")
    @Operation(summary = "스케쥴 아이템 생성", description = "특정 스케쥴에 아이템을 생성하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공적으로 스케쥴 아이템 생성"),
            @ApiResponse(responseCode = "403", description = "스케줄에 대한 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 스케쥴을 찾을 수 없음")
    })
    public ResponseEntity<Void> createItem(
            @Parameter(description = "스케쥴 ID", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable UUID scheduleId,
            @RequestBody ScheduleItemCreateRequest request) {
        scheduleItemService.itemCreate(scheduleId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 기존 스케쥴 아이템의 정보를 수정합니다.
     *
     * @param request 수정할 스케쥴 아이템의 상세 정보를 담은 DTO (ID 포함)
     * @return 수정된 스케쥴 아이템의 고유 ID를 포함하는 HTTP 200 OK 응답
     */
    @PutMapping
    @Operation(summary = "스케쥴 아이템 수정", description = "특정 스케쥴 아이템을 수정하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 스케쥴 아이템 수정"),
            @ApiResponse(responseCode = "403", description = "스케줄 아이템에 대한 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 스케쥴 아이템을 찾을 수 없음")
    })
    public ResponseEntity<UUID> updateItem(
            @RequestBody ScheduleItemUpdateRequest request) {
        UUID scheduleItemId = scheduleItemService.itemUpdate(request);
        return ResponseEntity.ok(scheduleItemId);
    }

    /**
     * 특정 스케쥴 아이템을 삭제합니다.
     *
     * @param scheduleItemId 삭제할 스케쥴 아이템의 고유 ID
     * @return 성공 시 HTTP 204 No Content 응답
     */
    @DeleteMapping("/{scheduleItemId}")
    @Operation(summary = "스케쥴 아이템 삭제", description = "특정 스케쥴의 특정 아이템을 삭제하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "성공적으로 스케쥴 아이템 삭제"),
            @ApiResponse(responseCode = "403", description = "스케줄 아이템에 대한 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 스케쥴 아이템을 찾을 수 없음")
    })
    public ResponseEntity<Void> deleteItem(
            @Parameter(description = "스케쥴 아이템 ID", example = "b7f23c9b-4567-4d89-9a12-c3b4d6a7f456")
            @PathVariable UUID scheduleItemId) {
        scheduleItemService.itemDelete(scheduleItemId);
        return ResponseEntity.noContent().build();
    }
}