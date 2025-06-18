package com.example.backend.mail.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class MailRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class mailSendRequest {
        private String email;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class mailVerifyRequest {
        private String token;
    }

}
