package com.example.backend.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * 스케줄 관련 API 응답을 위한 DTO 클래스들을 포함합니다.
 */
public class ScheduleResponse {

    /**
     * 스케줄 생성 응답 DTO입니다.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleCreateResponse {
        @Schema(description = "생성된 스케줄 ID", example = "9b9de25e-6a84-4b4e-b5e7-b81cdd90cc12")
        private UUID scheduleId;
    }

    /**
     * 스케줄 수정 응답 DTO입니다.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleUpdateResponse {
        @Schema(description = "업데이트된 스케줄 ID", example = "9b9de25e-6a84-4b4e-b5e7-b81cdd90cc12")
        private UUID scheduleId;
    }

    /**
     * 스케줄 목록 조회 시 사용되는 기본 정보 DTO입니다.
     */
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
        @Schema(description = "스케쥴 스타일", example = "휴양")
        private String scheduleStyle;
    }

    /**
     * 스케줄 상세 정보 조회 응답 DTO입니다.
     */
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
        @Schema(description = "스케줄에 포함된 아이템 목록")
        private List<scheduleItemInfo> scheduleItems;

    }

    /**
     * 스케줄에 포함된 개별 아이템의 상세 정보 DTO입니다.
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleItemInfo {
        @Schema(description = "아이템 ID", example = "d3f12c9b-4567-4d89-9a12-c3b4d6a7f456")
        private UUID scheduleItemId;
        @Schema(description = "장소 ID", example = "a1b2c3d4-5678-9101-1121-314151617181")
        private String contentId;
        @Schema(description = "제목", example = "126508")
        private String title;
        @Schema(description = "일차 번호", example = "1")
        private Integer dayNumber;
        @Schema(description = "시작 시간", example = "10:00:00")
        private LocalTime startTime;
        @Schema(description = "종료 시간", example = "12:00:00")
        private LocalTime endTime;
        @Schema(description = "메모", example = "점심 식사")
        private String memo;
        @Schema(description = "비용", example = "50000")
        private int cost;
        @Schema(description = "순서", example = "1")
        private int order;
        @Schema(description = "투어에 있는 테마", example = "테마")
        private String tema;
        @Schema(description = "지역명", example = "전주")
        private String regionName;
    }
}