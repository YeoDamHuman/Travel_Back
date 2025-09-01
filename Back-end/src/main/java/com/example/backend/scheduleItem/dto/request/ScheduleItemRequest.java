package com.example.backend.scheduleItem.dto.request;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.scheduleItem.entity.ScheduleItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.UUID;

/**
 * 스케쥴 아이템 관련 요청 DTO들을 모아둔 클래스.
 */
public class ScheduleItemRequest {

    /**
     * 스케쥴 아이템 생성을 위한 요청 DTO.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleItemCreateRequest {
        @Schema(description = "장소 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private String contentId;
        @Schema(description = "일정의 며칠째인지 (1일부터 시작)", example = "2")
        private Integer dayNumber;
        @Schema(description = "일정 시작 시간 (HH:mm:ss 형식)", example = "09:30:00")
        private LocalTime startTime;
        @Schema(description = "일정 종료 시간 (HH:mm:ss 형식)", example = "11:00:00")
        private LocalTime endTime;
        @Schema(description = "일정 메모", example = "오전 회의 및 준비 시간")
        private String memo;
        @Schema(description = "예상 비용", example = "15000")
        private int cost;
        @Schema(description = "순서", example = "1")
        private int order;

        /**
         * DTO를 ScheduleItem 엔티티로 변환합니다.
         *
         * @param request  스케쥴 아이템 생성 요청 DTO
         * @param schedule 연관된 스케쥴 엔티티
         * @return 생성된 ScheduleItem 엔티티
         */
        public static ScheduleItem toEntity(ScheduleItemCreateRequest request, Schedule schedule) {
            return ScheduleItem.builder().
                    contentId(request.getContentId()).
                    dayNumber(request.getDayNumber()).
                    startTime(request.getStartTime()).
                    endTime(request.getEndTime()).
                    memo(request.getMemo()).
                    cost(request.getCost()).
                    scheduleId(schedule).
                    order(request.getOrder()).
                    build();
        }
    }

    /**
     * 스케쥴 아이템 수정을 위한 요청 DTO.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleItemUpdateRequest {
        @Schema(description = "스케쥴 아이템 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID scheduleItemId;
        @Schema(description = "장소 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private String contentId;
        @Schema(description = "일정의 며칠째인지 (1일부터 시작)", example = "2")
        private int dayNumber;
        @Schema(description = "일정 시작 시간 (HH:mm:ss 형식)", example = "09:30:00")
        private LocalTime startTime;
        @Schema(description = "일정 종료 시간 (HH:mm:ss 형식)", example = "11:00:00")
        private LocalTime endTime;
        @Schema(description = "일정 메모", example = "오전 회의 및 준비 시간")
        private String memo;
        @Schema(description = "예상 비용", example = "15000")
        private int cost;
        @Schema(description = "순서", example = "1")
        private int order;

        /**
         * DTO의 정보를 기반으로 새로운 ScheduleItem 엔티티를 생성합니다.
         *
         * @param request 스케쥴 아이템 수정 요청 DTO
         * @return 생성된 ScheduleItem 엔티티
         */
        public static ScheduleItem toEntity(ScheduleItemUpdateRequest request) {
            return ScheduleItem.builder().
                    scheduleItemId(request.getScheduleItemId()).
                    contentId(request.getContentId()).
                    dayNumber(request.getDayNumber()).
                    startTime(request.getStartTime()).
                    endTime(request.getEndTime()).
                    memo(request.getMemo()).
                    cost(request.getCost()).
                    order(request.getOrder()).
                    build();
        }
    }
}