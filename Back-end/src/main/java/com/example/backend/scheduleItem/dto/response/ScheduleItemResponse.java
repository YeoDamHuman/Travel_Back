package com.example.backend.scheduleItem.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

public class ScheduleItemResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleItemCreateResponse {
        @Schema(description = "장소 ID (UUID)", example = "9b9de25e-6a84-4b4e-b5e7-b81cdd90cc12")
        private UUID placeId;
        @Schema(description = "응답 메시지", example = "스케줄 아이템 생성 성공.")
        private String message;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleItemUpdateResponse {
        @Schema(description = "스케쥴 아이템 ID (UUID)", example = "9b9de25e-6a84-4b4e-b5e7-b81cdd90cc12")
        private UUID scheduleItemId;
        @Schema(description = "응답 메시지", example = "스케줄 아이템 수정 성공.")
        private String message;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class scheduleItemDeleteResponse {
        @Schema(description = "응답 메시지", example = "스케줄 아이템 삭제 성공.")
        private String message;
    }
}
