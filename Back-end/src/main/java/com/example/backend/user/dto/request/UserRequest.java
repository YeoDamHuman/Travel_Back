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
        @Schema(description = "프로필 이미지 URL", example = "https://example.com/images/profile/asgjklasjdg.jpg")
        private String userProfileImage;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class updateRequest {
        @Schema(description = "변경할 이메일", example = "newemail@example.com", nullable = true)
        private String email;
        @Schema(description = "변경할 비밀번호", example = "newpassword1234", nullable = true)
        private String password;
        @Schema(description = "변경할 유저 이름", example = "김코딩", nullable = true)
        private String userName;
        @Schema(description = "변경할 닉네임", example = "자바의 신", nullable = true)
        private String userNickname;
        @Schema(description = "변경할 프로필 이미지 URL", example = "https://example.com/images/profile/abcd.jpg", nullable = true)
        private String userProfileImage;
    }
}
