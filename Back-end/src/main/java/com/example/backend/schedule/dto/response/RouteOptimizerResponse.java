package com.example.backend.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RouteOptimizerResponse {

    @Schema(description = "스케줄 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID scheduleId;

    @Schema(description = "최적화된 순서가 적용된 최종 아이템 목록")
    private List<OptimizedScheduleItem> scheduleItems;

    /**
     * 최종 순서가 포함된 개별 아이템
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class OptimizedScheduleItem {
        @Schema(description = "일자별 방문 순서", example = "1")
        private int order;

        @Schema(description = "콘텐츠 ID", example = "126508")
        private String contentId;

        @Schema(description = "여행일차", example = "1")
        private int dayNumber;
    }
}