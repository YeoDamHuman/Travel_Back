package com.example.backend.scheduleItem.controller;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.service.ScheduleService;
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
 * 클라이언트의 스케쥴 아이템 생성, 수정, 삭제 요청을 받아서
 * {@link ScheduleItemService}를 통해 비즈니스 로직을 수행하고 응답을 반환합니다.
 */
@RestController
@RequestMapping("/schedule/items")
@RequiredArgsConstructor
@Tag(name = "ScheduleItemAPI", description = "스케쥴 아이템 관련 API")
public class ScheduleItemController {

    private final ScheduleItemService scheduleItemService;
    private final ScheduleService scheduleService;

    /**
     * 특정 스케쥴에 새로운 아이템을 생성합니다.
     *
     * @param scheduleId 아이템을 생성할 스케쥴의 고유 ID
     * @param request 생성할 스케쥴 아이템의 상세 정보를 담은 DTO
     * @return 성공 시 HTTP 201 Created 응답
     * @apiNote {@link ScheduleService#findScheduleById(UUID)}를 호출하여 스케줄을 찾고,
     * {@link ScheduleItemService#itemCreate(Schedule, ScheduleItemCreateRequest)}를 호출하여 스케쥴 아이템을 생성합니다.
     */
    @PostMapping("/create/{scheduleId}")
    @Operation(summary = "스케쥴 아이템 생성", description = "특정 스케쥴에 아이템을 생성하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공적으로 스케쥴 아이템 생성"),
            @ApiResponse(responseCode = "404", description = "해당 스케쥴을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    public ResponseEntity<?> createItem(
            @Parameter(description = "스케쥴 ID", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable UUID scheduleId,
            @RequestBody ScheduleItemCreateRequest request) {
        Schedule schedule = scheduleService.findScheduleById(scheduleId);
        scheduleItemService.itemCreate(schedule, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 기존 스케쥴 아이템의 정보를 수정합니다.
     *
     * @param item 수정할 스케쥴 아이템의 상세 정보를 담은 DTO (ID 포함)
     * @return 수정된 스케쥴 아이템의 고유 ID를 포함하는 HTTP 200 OK 응답
     * @apiNote {@link ScheduleItemService#itemUpdate(ScheduleItemUpdateRequest)}를 호출하여 스케쥴 아이템을 수정합니다.
     */
    @PutMapping("/update")
    @Operation(summary = "스케쥴 아이템 수정", description = "특정 스케쥴 아이템을 수정하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 스케쥴 아이템 수정"),
            @ApiResponse(responseCode = "404", description = "해당 스케쥴 아이템을 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    public ResponseEntity<?> updateItem(
            @RequestBody ScheduleItemUpdateRequest item) {
        UUID scheduleItemId = scheduleItemService.itemUpdate(item);
        return ResponseEntity.ok(scheduleItemId);
    }

    /**
     * 특정 스케쥴 아이템을 삭제합니다.
     *
     * @param scheduleItemId 삭제할 스케쥴 아이템의 고유 ID
     * @return 성공 시 HTTP 204 No Content 응답
     * @apiNote {@link ScheduleItemService#itemDelete(UUID)}를 호출하여 스케쥴 아이템을 삭제합니다.
     */
    @DeleteMapping("/delete/{scheduleId}/{scheduleItemId}")
    @Operation(summary = "스케쥴 아이템 삭제", description = "특정 스케쥴의 특정 아이템을 삭제하는 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "성공적으로 스케쥴 아이템 삭제"),
            @ApiResponse(responseCode = "404", description = "해당 스케쥴 또는 스케쥴 아이템을 찾을 수 없음")
    })
    public ResponseEntity<?> deleteItem(
            @Parameter(description = "스케쥴 ID", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable UUID scheduleId,
            @Parameter(description = "스케쥴 아이템 ID", example = "b7f23c9b-4567-4d89-9a12-c3b4d6a7f456")
            @PathVariable UUID scheduleItemId) {

        scheduleItemService.itemDelete(scheduleId, scheduleItemId);
        return ResponseEntity.noContent().build();
    }

}