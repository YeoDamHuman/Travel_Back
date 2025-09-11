package com.example.backend.schedule.controller;

import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleCreateRequest;
import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleUpdateRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse;
import com.example.backend.schedule.dto.response.ScheduleResponse.ScheduleListInfo; // ✨ 임포트 변경
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleCreateResponse;
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleUpdateResponse;
import com.example.backend.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@Tag(name = "ScheduleAPI", description = "스케쥴 관련 API.")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // ... create, update, leaveSchedule 등 다른 엔드포인트는 변경 없음 ...
    @PostMapping("/create")
    public ResponseEntity<scheduleCreateResponse> create(
            @RequestBody ScheduleCreateRequest request) {

        UUID scheduleId = scheduleService.createSchedule(request);

        return ResponseEntity.ok(scheduleCreateResponse.builder()
                .scheduleId(scheduleId)
                .build());
    }

    @PutMapping("/update")
    public ResponseEntity<scheduleUpdateResponse> update(
            @RequestBody ScheduleUpdateRequest request) {
        UUID scheduleId = scheduleService.updateSchedule(request);
        return ResponseEntity.ok(scheduleUpdateResponse.builder()
                .scheduleId(scheduleId)
                .build());
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> leaveSchedule(
            @Parameter(description = "나갈 스케쥴 ID", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable UUID scheduleId) {
        scheduleService.leaveSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 현재 사용자의 스케줄 목록을 조회합니다.
     */
    @GetMapping
    @Operation(summary = "내 스케쥴 목록 조회", description = "현재 로그인한 사용자의 스케쥴 목록을 조회하는 API.")
    public ResponseEntity<List<ScheduleListInfo>> getSchedules() { // ✨ 반환 타입 변경
        List<ScheduleListInfo> schedules = scheduleService.getSchedules();
        return ResponseEntity.ok(schedules);
    }

    /**
     * 특정 스케줄의 상세 정보를 조회합니다. (통합 API)
     */
    @GetMapping("/details/{scheduleId}")
    @Operation(summary = "스케쥴 상세 조회 (통합)", description = "로그인 여부와 관계없이 스케쥴 상세 정보를 조회합니다. 참여자일 경우 편집 가능 여부가 반환됩니다.")
    public ResponseEntity<ScheduleResponse.ScheduleDetailResponse> getScheduleDetail(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        ScheduleResponse.ScheduleDetailResponse schedule = scheduleService.getScheduleDetail(scheduleId);
        return ResponseEntity.ok(schedule);
    }

    @PostMapping("/optimize/{scheduleId}")
    public ResponseEntity<Void> optimizeSchedule(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        scheduleService.optimizeRoute(scheduleId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{scheduleId}/join")
    public ResponseEntity<Void> joinSchedule(
            @Parameter(description = "참여할 스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        scheduleService.joinSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{scheduleId}/count")
    public ResponseEntity<Integer> getScheduleUserCount(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        int userCount = scheduleService.countScheduleUsers(scheduleId);
        return ResponseEntity.ok(userCount);
    }
}