package com.example.backend.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class RouteOptimizerRequest {

    @Schema(description = "스케줄 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID scheduleId;

    @Schema(description = "AI가 날짜별로 그룹화한 계획 목록")
    private List<DailyPlan> dailyPlans;

    /**
     * 날짜별 계획
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class DailyPlan {
        @Schema(description = "여행일차", example = "1")
        private int dayNumber;

        @Schema(description = "해당일에 방문할 장소 목록")
        private List<PlaceInfo> items;
    }

    /**
     * 개별 장소 정보
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class PlaceInfo {
        @Schema(description = "콘텐츠 ID", example = "126508")
        private String contentId;

        @Schema(description = "장소명", example = "경복궁")
        private String title;

        @Schema(description = "위도", example = "37.579617")
        private double latitude;

        @Schema(description = "경도", example = "126.977041")
        private double longitude;

        @Schema(description = "카테고리", example = "TOURIST_SPOT")
        private String category;
    }
}