package com.example.backend.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduleResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleCreateResponse {
        @Schema(description = "응답 메시지", example = "스케쥴이 성공적으로 생성되었습니다.")
        private String message;
        @Schema(description = "생성된 스케줄 ID", example = "9b9de25e-6a84-4b4e-b5e7-b81cdd90cc12")
        private UUID scheduleId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleUpdateResponse {
        @Schema(description = "응답 메시지", example = "스케쥴이 성공적으로 업데이트되었습니다.")
        private String message;
        @Schema(description = "업데이트된 스케줄 ID", example = "9b9de25e-6a84-4b4e-b5e7-b81cdd90cc12")
        private UUID scheduleId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleDeleteResponse {
        @Schema(description = "응답 메시지", example = "스케쥴이 성공적으로 삭제되었습니다.")
        private String message;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleInfo {
        @Schema(description = "스케줄 ID", example = "9b9de25e-6a84-4b4e-b5e7-b81cdd90cc12")
        private UUID scheduleId;
        @Schema(description = "스케줄 이름", example = "여름 휴가 계획")
        private String scheduleName;
        @Schema(description = "시작 날짜 (YYYY-MM-DD)", example = "2025-07-01")
        private LocalDate startDate;
        @Schema(description = "종료 날짜 (YYYY-MM-DD)", example = "2025-07-10")
        private LocalDate endDate;
        @Schema(description = "생성 일시 (YYYY-MM-DDTHH:mm:ss)", example = "2025-06-23T14:30:00")
        private LocalDateTime createdAt;
        @Schema(description = "수정 일시 (YYYY-MM-DDTHH:mm:ss)", example = "2025-06-24T10:20:00")
        private LocalDateTime updatedAt;
        @Schema(description = "예산 (단위: 원)", example = "1500000")
        private BigInteger budget;
        @Schema(description = "그룹 ID (그룹 스케줄인 경우)", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
        private UUID groupId;
        @Schema(description = "그룹 이름", example = "친구 여행 그룹")
        private String groupName;
        @Schema(description = "스케줄 생성자 사용자 ID", example = "4f8e123b-9876-4cde-b123-6f7890e12345")
        private UUID userId;
        @Schema(description = "스케줄 타입", example = "GROUP")
        private String scheduleType;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleDetailResponse {
        @Schema(description = "스케줄 ID", example = "9b9de25e-6a84-4b4e-b5e7-b81cdd90cc12")
        private UUID scheduleId;
        @Schema(description = "스케줄 이름", example = "여름 휴가 계획")
        private String scheduleName;
        @Schema(description = "시작 날짜 (YYYY-MM-DD)", example = "2025-07-01")
        private LocalDate startDate;
        @Schema(description = "종료 날짜 (YYYY-MM-DD)", example = "2025-07-10")
        private LocalDate endDate;
        @Schema(description = "생성 일시 (YYYY-MM-DDTHH:mm:ss)", example = "2025-06-23T14:30:00")
        private LocalDateTime createdAt;
        @Schema(description = "수정 일시 (YYYY-MM-DDTHH:mm:ss)", example = "2025-06-24T10:20:00")
        private LocalDateTime updatedAt;
        @Schema(description = "예산 (단위: 원)", example = "1500000")
        private BigInteger budget;
    }
}
