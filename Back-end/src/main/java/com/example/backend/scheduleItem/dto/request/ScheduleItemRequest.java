package com.example.backend.scheduleItem.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigInteger;
import java.sql.Time;
import java.util.UUID;

public class ScheduleItemRequest {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleUpdateRequest {
        private UUID placeId;
        private Integer dayNumber;
        private Time startTime;
        private Time endTime;
        private String memo;
        private BigInteger cost;
    }
}
