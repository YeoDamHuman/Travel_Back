package com.example.backend.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
public class UserResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class registerResponse {
        private String message;
    }
}
