package com.example.backend.mail.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class MailResponse {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class mailSendResponse {
        private String message;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class mailVerifyResponse {
        private String message;
    }
}
