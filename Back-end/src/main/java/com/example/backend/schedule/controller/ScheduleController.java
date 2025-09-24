package com.example.backend.schedule.controller;

import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleCreateRequest;
import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleUpdateRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse;
import com.example.backend.schedule.dto.response.ScheduleResponse.ScheduleId;
import com.example.backend.schedule.dto.response.ScheduleResponse.ScheduleListInfo;
import com.example.backend.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 스케줄 관련 작업을 관리하는 컨트롤러입니다.
 * 이 클래스는 스케줄 생성, 수정, 삭제 및 조회 기능을 위한 REST 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@Tag(name = "ScheduleAPI", description = "스케쥴 관련 API.")
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 새로운 스케줄을 생성합니다.
     * @param request 스케줄 생성에 필요한 상세 정보.
     * @return 생성된 스케줄의 ID (UUID).
     */
    @PostMapping("/create")
    @Operation(summary = "스케쥴 생성", description = "새로운 스케쥴을 생성하고 생성된 ID를 반환합니다.")
    public ResponseEntity<ScheduleId> create(
            @RequestBody ScheduleCreateRequest request) {
        UUID scheduleId = scheduleService.createSchedule(request);
        return ResponseEntity.ok(ScheduleId.builder()
                .scheduleId(scheduleId)
                .build());
    }

    /**
     * 기존 스케줄을 수정합니다.
     * @param request 스케줄 ID와 수정할 상세 정보.
     * @return 수정된 스케줄의 ID (UUID).
     */
    @PutMapping("/update")
    @Operation(summary = "스케쥴 업데이트", description = "스케쥴을 수정하고 수정된 ID를 반환합니다.")
    public ResponseEntity<ScheduleId> update(
            @RequestBody ScheduleUpdateRequest request) {
        UUID scheduleId = scheduleService.updateSchedule(request);
        return ResponseEntity.ok(ScheduleId.builder()
                .scheduleId(scheduleId)
                .build());
    }

    /**
     * 스케줄에서 나갑니다.
     * 현재 사용자를 스케줄 참여자 목록에서 제외하며, 마지막 참여자일 경우 스케줄이 삭제됩니다.
     * @param scheduleId 나갈 스케줄의 ID.
     * @return 작업 성공 시 내용 없는 응답.
     */
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "스케쥴 나가기", description = "현재 사용자를 스케쥴에서 제외시킵니다. 마지막 참여자일 경우 스케쥴이 삭제됩니다.")
    public ResponseEntity<Void> leaveSchedule(
            @Parameter(description = "나갈 스케쥴 ID", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable UUID scheduleId) {
        scheduleService.leaveSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 현재 사용자가 참여하고 있는 스케줄 목록을 조회합니다.
     * @return 스케줄 정보 목록.
     */
    @GetMapping
    @Operation(summary = "내 스케쥴 목록 조회", description = "현재 로그인한 사용자의 스케쥴 목록을 조회하는 API.")
    public ResponseEntity<List<ScheduleListInfo>> getSchedules() {
        List<ScheduleListInfo> schedules = scheduleService.getSchedules();
        return ResponseEntity.ok(schedules);
    }

    /**
     * 특정 스케줄의 상세 정보를 조회합니다. (통합 API)
     * 로그인 여부와 관계없이 누구나 호출할 수 있으며, 참여자일 경우 편집 가능 여부가 반환됩니다.
     * @param scheduleId 조회할 스케줄의 ID.
     * @return 스케줄의 상세 정보.
     */
    @GetMapping("/details/{scheduleId}")
    @Operation(summary = "스케쥴 상세 조회 (통합)", description = "로그인 여부와 관계없이 스케쥴 상세 정보를 조회합니다. 참여자일 경우 편집 가능 여부가 반환됩니다.")
    public ResponseEntity<ScheduleResponse.ScheduleDetailResponse> getScheduleDetail(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        ScheduleResponse.ScheduleDetailResponse schedule = scheduleService.getScheduleDetail(scheduleId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * AI 서비스를 사용하여 스케줄의 최적 경로를 계산합니다.
     * @param scheduleId 최적화할 스케줄의 ID.
     * @return 작업 성공 시 내용 없는 응답.
     */
    @PostMapping("/optimize/{scheduleId}")
    @Operation(summary = "최적 동선", description = "스케쥴 최적 동선을 위해 AI 서비스를 사용하는 API.")
    public ResponseEntity<Void> optimizeSchedule(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        scheduleService.optimizeRoute(scheduleId);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 스케줄에 현재 사용자를 참여자로 추가합니다.
     * @param scheduleId 참여할 스케줄의 ID.
     * @return 작업 성공 시 내용 없는 응답.
     */
    @PostMapping("/{scheduleId}/join")
    @Operation(summary = "스케쥴에 참여 (초대 수락)", description = "현재 로그인한 사용자를 특정 스케줄의 참여자로 추가합니다.")
    public ResponseEntity<Void> joinSchedule(
            @Parameter(description = "참여할 스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        scheduleService.joinSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 스케줄의 참여자 수를 조회합니다.
     * @param scheduleId 참여자 수를 조회할 스케줄의 ID.
     * @return 스케줄 참여자 수.
     */
    @GetMapping("/{scheduleId}/count")
    @Operation(summary = "스케쥴 참여자 수 조회", description = "특정 스케쥴에 참여하고 있는 사용자의 수를 조회하는 API.")
    public ResponseEntity<Integer> getScheduleUserCount(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        int userCount = scheduleService.countScheduleUsers(scheduleId);
        return ResponseEntity.ok(userCount);
    }
}