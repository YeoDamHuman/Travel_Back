package com.example.backend.schedule.controller;

import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleCreateRequest;
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
     * 이 엔드포인트는 요청 본문에 제공된 정보를 바탕으로 개인 스케줄을 생성합니다.
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
     * 스케줄에서 나갑니다.
     * <p>
     * 현재 사용자를 스케줄 참여자 목록에서 제외합니다.
     * 만약 마지막 참여자일 경우, 스케줄은 영구적으로 삭제됩니다.
     *
     * @param scheduleId 나갈 스케줄의 ID.
     * @return 성공적인 처리를 의미하는 내용 없는 {@link ResponseEntity}.
     */
    @DeleteMapping("/{scheduleId}")
    @Operation(summary = "스케쥴 나가기", description = "현재 사용자를 스케쥴에서 제외시킵니다. 마지막 참여자일 경우 스케쥴이 삭제됩니다.")
    public ResponseEntity<Void> leaveSchedule(
            @Parameter(description = "나갈 스케쥴 ID", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
            @PathVariable UUID scheduleId) {
        scheduleService.leaveSchedule(scheduleId); // 서비스의 새 메서드 호출
        return ResponseEntity.noContent().build();
    }

    /**
     * 현재 사용자의 스케줄 목록을 조회합니다.
     * <p>
     * 이 엔드포인트는 현재 로그인한 사용자의 모든 개인 스케줄 목록을 가져옵니다.
     *
     * @return {@link scheduleInfo} 객체들의 목록을 담은 {@link ResponseEntity}.
     */
    @GetMapping
    @Operation(summary = "내 스케쥴 목록 조회", description = "현재 로그인한 사용자의 스케쥴 목록을 조회하는 API.")
    public ResponseEntity<List<scheduleInfo>> getSchedules() {
        List<scheduleInfo> schedules = scheduleService.getSchedules();
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
    @GetMapping("/details/{scheduleId}")
    @Operation(summary = "스케쥴 상세 조회", description = "스케쥴 상세 정보를 조회하는 API.")
    public ResponseEntity<ScheduleResponse.scheduleDetailResponse> getScheduleDetail(
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
    public ResponseEntity<Void> optimizeSchedule(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        scheduleService.optimizeRoute(scheduleId);
        return ResponseEntity.ok().build();
    }

    /**
     * 특정 스케줄의 상세 정보를 공개적으로 조회합니다.
     * <p>
     * 이 엔드포인트는 인증 없이 스케줄 ID만으로 상세 정보를 조회할 수 있습니다.
     *
     * @param scheduleId 조회할 스케줄의 ID.
     * @return 상세 스케줄 정보가 담긴 {@link ScheduleResponse.scheduleDetailResponse} 객체를 담은 {@link ResponseEntity}.
     */
    @GetMapping("/public/{scheduleId}")
    @Operation(summary = "공개 스케쥴 상세 조회", description = "로그인 없이 스케쥴 상세 정보를 조회하는 API.")
    public ResponseEntity<ScheduleResponse.scheduleDetailResponse> getPublicScheduleDetail(
            @Parameter(description = "스케쥴 ID", example = "b4e8f9a0-1234-4c56-8d7e-9f12345b6789")
            @PathVariable UUID scheduleId) {
        ScheduleResponse.scheduleDetailResponse schedule = scheduleService.getPublicScheduleDetail(scheduleId);
        return ResponseEntity.ok(schedule);
    }

    /**
     * 특정 스케줄에 현재 사용자를 참여자로 추가합니다. (초대 수락)
     * <p>
     * 이 엔드포인트는 초대 링크를 통해 접근하는 등, 사용자가 스케줄에 참여 의사를 밝혔을 때 사용됩니다.
     *
     * @param scheduleId 참여할 스케줄의 ID.
     * @return 성공적인 참여를 의미하는 OK 상태의 {@link ResponseEntity}.
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
     * <p>
     * 이 엔드포인트는 해당 스케줄에 참여하고 있는 총 사용자 수를 반환합니다.
     *
     * @param scheduleId 참여자 수를 조회할 스케줄의 ID.
     * @return 참여자 수를 담은 {@link ResponseEntity}.
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