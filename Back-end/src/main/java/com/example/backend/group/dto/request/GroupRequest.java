package com.example.backend.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class GroupRequest {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class groupCreateRequest {
        @Schema(description = "그룹 이름", example = "딩딩디기디동")
        private String groupName;
    }
}
