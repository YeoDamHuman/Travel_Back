package com.example.backend.schedule.dto.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

public class ScheduleRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleCreateRequest {
        private String scheduleName;
        private LocalDate startDate;
        private LocalDate endDate;
        private BigInteger budget;
        private UUID groupId;
    }
}
