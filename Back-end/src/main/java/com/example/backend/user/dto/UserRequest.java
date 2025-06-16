package com.example.backend.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UserRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class loginRequest {
        private String email;
        private String password;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class registerRequest {
        private String userName;
        private String email;
        private String password;
        private String address;
    }
}
