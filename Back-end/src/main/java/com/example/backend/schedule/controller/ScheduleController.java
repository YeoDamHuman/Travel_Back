package com.example.backend.schedule.controller;

import com.example.backend.schedule.dto.request.ScheduleRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse;
import com.example.backend.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
@Tag(name = "ScheduleAPI", description = "스케쥴 관련 API.")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 1️⃣ 스케쥴 생성
    @PostMapping("/create")
    @Operation(summary = "스케쥴 생성", description = "새로운 스케쥴을 생성하는 API.")
    public ResponseEntity<ScheduleResponse.scheduleCreateResponse> create(
            @RequestBody ScheduleRequest.scheduleCreateRequest request) {

        UUID scheduleId = scheduleService.createSchedule(request);

        return ResponseEntity.ok(ScheduleResponse.scheduleCreateResponse.builder()
                .scheduleId(scheduleId)
                .message("스케쥴이 성공적으로 생성되었습니다.")
                .build());
    }

    // 2️⃣ 스케쥴 수정
    @PutMapping("/update")
    @Operation(summary = "스케쥴 업데이트", description = "스케쥴을 수정하는 API.")
    public ResponseEntity<ScheduleResponse.scheduleUpdateResponse> update(
            @RequestBody ScheduleRequest.scheduleUpdateRequest request) {
        UUID scheduleId = scheduleService.updateSchedule(request);
        return ResponseEntity.ok(ScheduleResponse.scheduleUpdateResponse.builder()
                .scheduleId(scheduleId)
                .message("스케쥴이 성공적으로 업데이트되었습니다.")
                .build());
    }

    // 3️⃣ 스케쥴 삭제
    @DeleteMapping("/delete")
    @Operation(summary = "스케쥴 삭제", description = "스케쥴을 삭제하는 API.")
    public ResponseEntity<ScheduleResponse.scheduleDeleteResponse> delete(
            @RequestBody ScheduleRequest.scheduleDeleteRequest request) {
        scheduleService.deleteSchedule(request.getScheduleId());
        return ResponseEntity.ok(
                ScheduleResponse.scheduleDeleteResponse.builder()
                        .message("스케쥴이 성공적으로 삭제되었습니다.")
                        .build()
        );
    }

    // 4️⃣ 스케쥴 목록 조회
    @GetMapping({"", "/{groupId}"}) // 두 가지 경로를 모두 매핑
    @Operation(summary = "스케쥴 목록 조회", description = "개인 또는 그룹 스케쥴 목록을 조회하는 API.")
    public ResponseEntity<List<ScheduleResponse.scheduleInfo>> getSchedules(
            @Parameter(description = "그룹 ID (선택적)", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable(required = false) UUID groupId) {
        List<ScheduleResponse.scheduleInfo> schedules = scheduleService.getSchedules(groupId);
        return ResponseEntity.ok(schedules);
    }

    // 5️⃣ 스케쥴 상세 조회
    @GetMapping("/{scheduleId}")
    @Operation(summary = "스케쥴 상세 조회", description = "스케쥴 상세 정보를 조회하는 API.")
    public ResponseEntity<ScheduleResponse.scheduleDetailResponse> getSchedule(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        ScheduleResponse.scheduleDetailResponse schedule = scheduleService.getScheduleDetail(scheduleId);
        return ResponseEntity.ok(schedule);
    }

    // 6️⃣ 경로 최적화
    @PostMapping("/{scheduleId}/optimize")
    @Operation(summary = "최적 동선", description = "스케쥴 최적 동선을 위해 gpt 사용하는 API.")
    public Mono<ResponseEntity<ScheduleResponse.OptimizeRouteResponse>> optimizeSchedule(@PathVariable UUID scheduleId, @RequestBody ScheduleRequest.OptimizeRouteRequest request) {
        return scheduleService.optimizeRoute(scheduleId, request)
                .map(response -> ResponseEntity.ok(response));
    }

}
