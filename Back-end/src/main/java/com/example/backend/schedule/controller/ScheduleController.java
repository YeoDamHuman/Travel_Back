package com.example.backend.schedule.controller;

import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleCreateRequest;
import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleDeleteRequest;
import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleUpdateRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse;
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleCreateResponse;
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleInfo;
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
     * <p>
     * 이 엔드포인트는 요청 본문에 제공된 정보를 바탕으로 개인 또는 그룹 스케줄을 생성합니다.
     *
     * @param request 스케줄 상세 정보가 담긴 {@link ScheduleCreateRequest} 객체.
     * @return 새로 생성된 스케줄의 ID를 포함하는 {@link scheduleCreateResponse} 객체를 담은 {@link ResponseEntity}.
     */
    @PostMapping("/create")
    @Operation(summary = "스케쥴 생성", description = "새로운 스케쥴을 생성하는 API.")
    public ResponseEntity<scheduleCreateResponse> create(
            @RequestBody ScheduleCreateRequest request) {

        UUID scheduleId = scheduleService.createSchedule(request);

        return ResponseEntity.ok(scheduleCreateResponse.builder()
                .scheduleId(scheduleId)
                .build());
    }

    /**
     * 기존 스케줄을 수정합니다.
     * <p>
     * 이 엔드포인트를 통해 기존 스케줄의 정보를 수정할 수 있습니다.
     *
     * @param request 스케줄 ID와 수정할 상세 정보가 담긴 {@link ScheduleUpdateRequest} 객체.
     * @return 수정된 스케줄의 ID를 포함하는 {@link scheduleUpdateResponse} 객체를 담은 {@link ResponseEntity}.
     */
    @PutMapping("/update")
    @Operation(summary = "스케쥴 업데이트", description = "스케쥴을 수정하는 API.")
    public ResponseEntity<scheduleUpdateResponse> update(
            @RequestBody ScheduleUpdateRequest request) {
        UUID scheduleId = scheduleService.updateSchedule(request);
        return ResponseEntity.ok(scheduleUpdateResponse.builder()
                .scheduleId(scheduleId)
                .build());
    }

    /**
     * 스케줄을 ID를 기반으로 삭제합니다.
     * <p>
     * 이 엔드포인트는 시스템에서 스케줄을 영구적으로 삭제합니다.
     *
     * @param request 삭제할 스케줄의 ID가 담긴 {@link ScheduleDeleteRequest} 객체.
     * @return 성공적인 삭제를 의미하는 내용 없는 {@link ResponseEntity}.
     */
    @DeleteMapping("/delete")
    @Operation(summary = "스케쥴 삭제", description = "스케쥴을 삭제하는 API.")
    public ResponseEntity<?> delete(
            @RequestBody ScheduleDeleteRequest request) {
        scheduleService.deleteSchedule(request.getScheduleId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 스케줄 목록을 조회합니다.
     * <p>
     * 이 엔드포인트는 그룹 ID가 제공되지 않으면 개인 스케줄을, 그룹 ID가 제공되면 해당 그룹의 스케줄을 가져옵니다.
     *
     * @param groupId 그룹의 ID. 선택적 매개변수입니다. null일 경우 개인 스케줄이 반환됩니다.
     * @return {@link scheduleInfo} 객체들의 목록을 담은 {@link ResponseEntity}.
     */
    @GetMapping({"", "/{groupId}"})
    @Operation(summary = "스케쥴 목록 조회", description = "개인 또는 그룹 스케쥴 목록을 조회하는 API.")
    public ResponseEntity<List<scheduleInfo>> getSchedules(
            @Parameter(description = "그룹 ID (선택적)", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable(required = false) UUID groupId) {
        List<scheduleInfo> schedules = scheduleService.getSchedules(groupId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * 특정 스케줄의 상세 정보를 조회합니다.
     * <p>
     * 이 엔드포인트는 스케줄에 포함된 모든 스케줄 아이템을 포함하여 전체적인 스케줄 정보를 반환합니다.
     *
     * @param scheduleId 조회할 스케줄의 ID.
     * @return 상세 스케줄 정보가 담긴 {@link ScheduleResponse.scheduleDetailResponse} 객체를 담은 {@link ResponseEntity}.
     */
    @GetMapping("details/{scheduleId}")
    @Operation(summary = "스케쥴 상세 조회", description = "스케쥴 상세 정보를 조회하는 API.")
    public ResponseEntity<ScheduleResponse.scheduleDetailResponse> getSchedule(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        ScheduleResponse.scheduleDetailResponse schedule = scheduleService.getScheduleDetail(scheduleId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * AI 서비스를 사용하여 스케줄의 최적 경로를 계산합니다.
     * <p>
     * 이 엔드포인트는 외부 AI 서비스를 호출하여 주어진 스케줄 아이템들의 순서와 시간을 최적화하여 가장 효율적인 동선을 제공합니다.
     *
     * @param scheduleId 최적화할 스케줄의 ID.
     * @return 최적화 과정이 시작되었음을 나타내는 OK 상태의 {@link ResponseEntity}.
     */
    @PostMapping("/optimize/{scheduleId}")
    @Operation(summary = "최적 동선", description = "스케쥴 최적 동선을 위해 gpt 사용하는 API.")
    public ResponseEntity<?> optimizeSchedule(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        scheduleService.optimizeRoute(scheduleId);
        return ResponseEntity.ok().build();
    }

    /**
     *  GraphHopper 서비스를 사용하여 스케줄의 최적 경로를 계산합니다.
     * <p>
     * 이 엔드포인트는 외부 AI 서비스를 호출하여 주어진 스케줄 아이템들의 순서와 시간을 최적화하여 가장 효율적인 동선을 제공합니다.
     *
     * @param scheduleId 최적화할 스케줄의 ID.
     * @return 최적화 과정이 시작되었음을 나타내는 OK 상태의 {@link ResponseEntity}.
     */
    @PostMapping("/optimize/test/{scheduleId}")
    @Operation(summary = "최적 동선", description = "스케쥴 최적 동선을 위해 GraphHopper를 사용하는 API.")
    public ResponseEntity<?> optimizeTestSchedule(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        scheduleService.optimizeTestRoute(scheduleId);
        return ResponseEntity.ok().build();
    }
}