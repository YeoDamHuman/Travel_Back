package com.example.backend.mail.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class MailResponse {
    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "응답 메시지", example = "메일 전송이 완료되었습니다.")
    public static class mailSendResponse {
        private String message;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "응답 메시지", example = "인증이 완료되었습니다.")
    public static class mailVerifyResponse {
        private String message;
    }
}
