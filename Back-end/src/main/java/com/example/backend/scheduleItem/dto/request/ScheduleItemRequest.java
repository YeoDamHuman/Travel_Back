package com.example.backend.scheduleItem.dto.request;

import com.example.backend.schedule.entity.Schedule;
import com.example.backend.scheduleItem.entity.ScheduleItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

public class ScheduleItemRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleItemCreateRequest {
        @Schema(description = "장소 ID", example = "126108")
        private String contentId;
        @Schema(description = "일정의 며칠째인지 (1일부터 시작)", example = "2")
        private int dayNumber;
        @Schema(description = "일정 메모", example = "오전 회의 및 준비 시간")
        private String memo;
        @Schema(description = "예상 비용", example = "15000")
        private int cost;
        @Schema(description = "순서", example = "1")
        private int order;

        public static ScheduleItem toEntity(ScheduleItemCreateRequest request, Schedule schedule) {
            return ScheduleItem.builder()
                    .contentId(request.getContentId())
                    .dayNumber(request.getDayNumber())
                    .memo(request.getMemo())
                    .cost(request.getCost())
                    .scheduleId(schedule)
                    .order(request.getOrder())
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleItemUpdateRequest {
        @Schema(description = "스케쥴 아이템 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID scheduleItemId;
        @Schema(description = "스케쥴 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID scheduleId;
        @Schema(description = "장소 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private String contentId;
        @Schema(description = "일정의 며칠째인지 (1일부터 시작)", example = "2")
        private int dayNumber;
        @Schema(description = "일정 메모", example = "오전 회의 및 준비 시간")
        private String memo;
        @Schema(description = "예상 비용", example = "15000")
        private int cost;
        @Schema(description = "순서", example = "1")
        private int order;

        public static ScheduleItem toEntity(ScheduleItemUpdateRequest request, Schedule schedule) {
            return ScheduleItem.builder()
                    .scheduleItemId(request.getScheduleItemId())
                    .scheduleId(schedule)
                    .contentId(request.getContentId())
                    .dayNumber(request.getDayNumber())
                    .memo(request.getMemo())
                    .cost(request.getCost())
                    .order(request.getOrder())
                    .build();
        }
    }
}