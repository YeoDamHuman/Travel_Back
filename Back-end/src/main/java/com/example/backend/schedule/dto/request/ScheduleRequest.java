package com.example.backend.schedule.dto.request;
import com.example.backend.schedule.entity.Schedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class ScheduleRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ScheduleCreateRequest {
        @Schema(description = "스케줄 이름", example = "여름 휴가 계획")
        private String scheduleName;
        @Schema(description = "시작 날짜 (YYYY-MM-DD)", example = "2025-07-01")
        private LocalDate startDate;
        @Schema(description = "종료 날짜 (YYYY-MM-DD)", example = "2025-07-10")
        private LocalDate endDate;
        @Schema(description = "예산 (단위: 원)", example = "1500000")
        private BigInteger budget;
        @Schema(description = "출발 장소", example = "서울역")
        private String startPlace;
        @Schema(description = "출발 시간", example = "09:00")
        private LocalTime startTime;
        @Schema(description = "스케줄 스타일 (여행 목적 등)", example = "쇼핑")
        private String scheduleStyle;
        @Schema(description = "일정 아이템 목록")
        private List<Items> scheduleItem;

        public static Schedule toEntity(ScheduleCreateRequest request) {
            return Schedule.builder()
                    .scheduleName(request.getScheduleName())
                    .startDate(request.startDate)
                    .endDate(request.endDate)
                    .budget(request.budget)
                    .startPlace(request.startPlace)
                    .startTime(request.startTime)
                    .scheduleStyle(request.scheduleStyle)
                    .build();
        }

        @Getter
        @Builder
        @AllArgsConstructor
        public static class Items {
            @Schema(description = "콘텐츠 ID", example = "126508")
            private String contentId;
            @Schema(description = "비용", example = "12000")
            private int cost;
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ScheduleUpdateRequest {
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
        @Schema(description = "시작 장소", example = "서울역")
        private String startPlace;

        /**
         * 요청 데이터(request)와 원본 데이터(originalSchedule)를 조합하여
         * 업데이트를 위한 새로운 Schedule 엔티티를 생성합니다.
         */
        public static Schedule toEntity(ScheduleUpdateRequest request, Schedule originalSchedule) {
            return Schedule.builder()
                    .scheduleId(originalSchedule.getScheduleId())
                    .scheduleName(request.getScheduleName())
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .budget(request.getBudget())
                    .startPlace(request.getStartPlace())
                    .createdAt(originalSchedule.getCreatedAt())
                    .scheduleStyle(originalSchedule.getScheduleStyle())
                    .startTime(originalSchedule.getStartTime())
                    .users(originalSchedule.getUsers())
                    .build();
        }
    }
}