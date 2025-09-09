package com.example.backend.scheduleItem.dto.request;

import com.example.backend.schedule.entity.Schedule;
import com.example.backend.scheduleItem.entity.ScheduleItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
        @Schema(description = "장소 ID", example = "126108")
        private String contentId;
        @Schema(description = "일정의 며칠째인지 (1일부터 시작)", example = "2")
        private Integer dayNumber;
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

    /**
     * 스케쥴 아이템 수정을 위한 요청 DTO.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleItemUpdateRequest {
        @Schema(description = "수정할 스케쥴 아이템 ID", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID scheduleItemId;
        @Schema(description = "일정의 며칠째인지 (1일부터 시작)", example = "3")
        private int dayNumber;
        @Schema(description = "일정 메모", example = "수정된 메모")
        private String memo;
        @Schema(description = "예상 비용", example = "20000")
        private int cost;
        @Schema(description = "순서", example = "2")
        private int order;

        /**
         * 요청 DTO와 원본 엔티티를 조합하여 업데이트를 위한 새로운 ScheduleItem 엔티티를 생성합니다.
         *
         * @param request      스케쥴 아이템 수정 요청 DTO
         * @param originalItem DB에서 조회한 원본 스케줄 아이템 엔티티
         * @return 업데이트된 정보를 담은 새로운 ScheduleItem 엔티티
         */
        public static ScheduleItem toEntity(ScheduleItemUpdateRequest request, ScheduleItem originalItem) {
            return ScheduleItem.builder()
                    .scheduleItemId(originalItem.getScheduleItemId()) // ID는 원본에서
                    .scheduleId(originalItem.getScheduleId())     // 스케줄 ID도 원본에서
                    .contentId(originalItem.getContentId())       // 장소 ID도 원본에서 (수정 불가 필드)
                    .dayNumber(request.getDayNumber())              // DTO에서 받은 값으로 업데이트
                    .memo(request.getMemo())                        // DTO에서 받은 값으로 업데이트
                    .cost(request.getCost())                        // DTO에서 받은 값으로 업데이트
                    .order(request.getOrder())                      // DTO에서 받은 값으로 업데이트
                    .build();
        }
    }
}