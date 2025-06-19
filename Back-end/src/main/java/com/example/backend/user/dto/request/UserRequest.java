package com.example.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UserRequest {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class loginRequest {
        @Schema(description = "사용자 이메일", example = "test@test.com")
        private String email;
        @Schema(description = "사용자 비밀번호", example = "test1234")
        private String password;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class registerRequest {
        @Schema(description = "유저 이름", example = "김재균")
        private String userName;
        @Schema(description = "사용자 이메일", example = "test@test.com")
        private String email;
        @Schema(description = "사용자 비밀번호", example = "test1234")
        private String password;
        @Schema(description = "사용자 닉네임", example = "광진구 총잡이 김재균")
        private String userNickname;
    }
}
