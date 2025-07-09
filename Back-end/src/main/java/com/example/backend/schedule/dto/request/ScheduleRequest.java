package com.example.backend.schedule.dto.request;

import com.example.backend.schedule.entity.ScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

public class ScheduleRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleCreateRequest {
        @Schema(description = "스케줄 이름", example = "여름 휴가 계획")
        private String scheduleName;
        @Schema(description = "시작 날짜 (YYYY-MM-DD)", example = "2025-07-01")
        private LocalDate startDate;
        @Schema(description = "종료 날짜 (YYYY-MM-DD)", example = "2025-07-10")
        private LocalDate endDate;
        @Schema(description = "예산 (단위: 원)", example = "1500000")
        private BigInteger budget;
        @Schema(description = "그룹 ID (그룹 스케줄인 경우 필수, 개인 스케줄인 경우 null)", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
        private UUID groupId;
        @Schema(description = "스케줄 타입 (PERSONAL, GROUP 중 하나)", example = "GROUP")
        private ScheduleType scheduleType;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleUpdateRequest {
        @Schema(description = "스케쥴 아이디", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID scheduleId;
        @Schema(description = "스케줄 이름", example = "여름 휴가 계획")
        private String scheduleName;
        @Schema(description = "시작 날짜 (YYYY-MM-DD)", example = "2025-07-01")
        private LocalDate startDate;
        @Schema(description = "종료 날짜 (YYYY-MM-DD)", example = "2025-07-10")
        private LocalDate endDate;
        @Schema(description = "예산 (단위: 원)", example = "1500000")
        private BigInteger budget;
        @Schema(description = "그룹 ID (그룹 스케줄인 경우 필수, 개인 스케줄인 경우 null)", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
        private UUID groupId;
        @Schema(description = "스케줄 타입 (PERSONAL, GROUP 중 하나)", example = "GROUP")
        private ScheduleType scheduleType;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleDeleteRequest {
        @Schema(description = "스케쥴 아이디", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID scheduleId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OptimizeRouteRequest {
        @Schema(description = "경로 최적화 타입 (예: 최단 거리, 최소 시간 등)", example = "최단 거리")
        private String optimizationType;
        @Schema(description = "경로 최적화에 대한 선호 옵션")
        private Preferences preferences;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class Preferences {
        @Schema(description = "교통수단 타입 (예: 자동차, 도보, 대중교통)", example = "자동차")
        private String transportationType;
        @Schema(description = "톨게이트 우회 여부", example = "true")
        private boolean avoidTolls;
        @Schema(description = "휴게소 포함 여부", example = "false")
        private boolean includeRestStops;
    }
}
