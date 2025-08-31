package com.example.backend.schedule.dto.request;

import com.example.backend.cart.entity.Cart;
import com.example.backend.group.entity.Group;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.entity.ScheduleType;
import com.example.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigInteger;
import java.time.LocalDate;
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
        @Schema(description = "그룹 ID (그룹 스케줄인 경우 필수, 개인 스케줄인 경우 null)", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
        private UUID groupId;
        @Schema(description = "스케줄 타입 (PERSONAL, GROUP 중 하나)", example = "GROUP")
        private ScheduleType scheduleType;
        @Schema(description = "스케줄 스타일 (여행 목적 등)", example = "쇼핑")
        private String scheduleStyle;
        @Schema(description = "출발 장소", example = "서울역")
        private String startPlace;
        @Schema(description = "출발 시간", example = "09:00")
        private String startTime;
        @Schema(description = "일정 아이템 목록")
        private List<Items> scheduleItem;

        public static Schedule toEntity(ScheduleCreateRequest request, Group group, User user, Cart cart) {
            return Schedule.builder()
                    .scheduleId(null)
                    .scheduleName(request.getScheduleName())
                    .startDate(request.startDate)
                    .endDate(request.endDate)
                    .createdAt(null)
                    .updatedAt(null)
                    .budget(request.budget)
                    .groupId(group)
                    .scheduleStyle(request.scheduleStyle)
                    .userId(user)
                    .scheduleType(request.scheduleType)
                    .cartId(cart)
                    .build();
        }

        @Getter
        @Builder
        @AllArgsConstructor
        public static class Items {
            @Schema(description = "콘텐츠 ID", example = "abcd1234")
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
        @Schema(description = "그룹 ID (그룹 스케줄인 경우 필수, 개인 스케줄인 경우 null)", example = "a3f12c9b-4567-4d89-9a12-c3b4d6a7f123")
        private UUID groupId;
        @Schema(description = "스케줄 타입 (PERSONAL, GROUP 중 하나)", example = "GROUP")
        private ScheduleType scheduleType;
        @Schema(description = "스케쥴 스타일", example = "휴양")
        private String scheduleStyle;

        public static Schedule toEntity(ScheduleUpdateRequest request, Schedule schedule, Group group) {
            return Schedule.builder()
                    .scheduleId(schedule.getScheduleId())
                    .budget(request.budget)
                    .createdAt(schedule.getCreatedAt())
                    .endDate(request.getEndDate())
                    .scheduleName(request.getScheduleName())
                    .scheduleStyle(request.getScheduleStyle())
                    .scheduleType(request.getScheduleType())
                    .startDate(request.getStartDate())
                    .cartId(schedule.getCartId())
                    .userId(schedule.getUserId())
                    .groupId(group)
                    .build();
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ScheduleDeleteRequest {
        @Schema(description = "스케쥴 아이디", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID scheduleId;
    }

}
