package com.example.backend.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UserResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "응답 메시지", example = "회원가입이 완료되었습니다.")
    public static class registerResponse {
        private String message;
    }
}
