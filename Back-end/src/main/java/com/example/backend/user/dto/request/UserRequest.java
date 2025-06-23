package com.example.backend.user.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

public class UserRequest {

    @Getter
    @Builder
    public static class loginRequest {
        @Schema(description = "사용자 이메일", example = "test@test.com")
        private final String email;

        @Schema(description = "사용자 비밀번호", example = "test1234")
        private final String password;

        @JsonCreator
        public loginRequest(
                @JsonProperty("email") String email,
                @JsonProperty("password") String password) {
            this.email = email;
            this.password = password;
        }
    }

    @Getter
    @Builder
    public static class registerRequest {
        @Schema(description = "유저 이름", example = "김재균")
        private final String userName;

        @Schema(description = "사용자 이메일", example = "test@test.com")
        private final String email;

        @Schema(description = "사용자 비밀번호", example = "test1234")
        private final String password;

        @Schema(description = "사용자 닉네임", example = "광진구 총잡이 김재균")
        private final String userNickname;

        @JsonCreator
        public registerRequest(
                @JsonProperty("userName") String userName,
                @JsonProperty("email") String email,
                @JsonProperty("password") String password,
                @JsonProperty("userNickname") String userNickname) {
            this.userName = userName;
            this.email = email;
            this.password = password;
            this.userNickname = userNickname;
        }
    }
}