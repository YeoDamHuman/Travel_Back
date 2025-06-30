package com.example.backend.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class GroupResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(name = "groupUserResponse", description = "그룹과 그룹에 속한 사용자 정보를 담은 응답.")
    public static class groupUserResponse {
        @Schema(description = "그룹 고유 아이디", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID groupId;
        @Schema(description = "그룹 이름", example = "딩딩디기디동")
        private String groupName;
        @Schema(description = "그룹 생성 일시", example = "2025-06-30T10:15:30")
        private LocalDateTime createdAt;
        @Schema(description = "그룹에 속한 사용자 목록")
        private Set<groupUser> users;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(name = "groupUser", description = "그룹에 속한 사용자 정보")
    public static class groupUser {
        @Schema(description = "사용자 고유 아이디", example = "a1b2c3d4-e5f6-7890-abcd-1234567890ab")
        private UUID userId;
        @Schema(description = "사용자 이름", example = "홍길동")
        private String userName;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(name = "groupCountingResponse", description = "특정 그룹에 속한 인원 수 정보")
    public static class groupCountingResponse {
        @Schema(description = "그룹 고유 아이디", example = "123e4567-e89b-12d3-a456-426614174000")
        private UUID groupId;
        @Schema(description = "그룹에 속한 인원 수", example = "5")
        private int memberCount;
    }
}
