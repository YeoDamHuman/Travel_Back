package com.example.backend.mail.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class MailRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class mailSendRequest {
        @Schema(description = "사용자 이메일", example = "test@test.com")
        private String email;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class mailVerifyRequest {
        @Schema(description = "인증 토큰", example = "ajladjfklasdjflkads")
        private String token;
    }

}
