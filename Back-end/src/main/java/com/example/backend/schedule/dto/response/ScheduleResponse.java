package com.example.backend.schedule.dto.response;

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
        private String message;
        private UUID scheduleId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleInfo {
        private UUID scheduleId;
        private String scheduleName;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private BigInteger budget;
        private UUID groupId;        // 그룹 ID도 포함하면 좋음
        private String groupName;    // 그룹 이름도 포함하면 편리
    }
}
