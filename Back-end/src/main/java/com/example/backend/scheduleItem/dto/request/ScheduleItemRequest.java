package com.example.backend.scheduleItem.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;
import java.time.LocalTime;
import java.util.UUID;

public class ScheduleItemRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleItemCreateRequest {
        @Schema(description = "장소 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID placeId;
        @Schema(description = "일정의 며칠째인지 (1일부터 시작)", example = "2")
        private Integer dayNumber;
        @Schema(description = "일정 시작 시간 (HH:mm:ss 형식)", example = "09:30:00")
        private LocalTime startTime;
        @Schema(description = "일정 종료 시간 (HH:mm:ss 형식)", example = "11:00:00")
        private LocalTime endTime;
        @Schema(description = "일정 메모", example = "오전 회의 및 준비 시간")
        private String memo;
        @Schema(description = "예상 비용", example = "15000")
        private BigInteger cost;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleItemUpdateRequest {
        @Schema(description = "스케쥴 아이템 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID scheduleItemId;
        @Schema(description = "장소 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID placeId;
        @Schema(description = "일정의 며칠째인지 (1일부터 시작)", example = "2")
        private Integer dayNumber;
        @Schema(description = "일정 시작 시간 (HH:mm:ss 형식)", example = "09:30:00")
        private LocalTime startTime;
        @Schema(description = "일정 종료 시간 (HH:mm:ss 형식)", example = "11:00:00")
        private LocalTime endTime;
        @Schema(description = "일정 메모", example = "오전 회의 및 준비 시간")
        private String memo;
        @Schema(description = "예상 비용", example = "15000")
        private BigInteger cost;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleItemDeleteRequest {
        @Schema(description = "스케쥴 아이템 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID scheduleItemId;
    }

}
