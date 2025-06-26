package com.example.backend.user.dto.response;

import com.example.backend.jwt.dto.JwtDto;
import com.example.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UserResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class loginResponse {
        @Schema(description = "JWT 토큰 정보")
        private JwtDto jwtDto;
        @Schema(description = "사용자 닉네임", example = "광진구 총잡이 김재균")
        private String userNickname;
        @Schema(description = "사용자 프로필 이미지 URL", example = "https://example.com/profile.png")
        private String userProfileImage;
        @Schema(description = "권한", example = "USER")
        private User.Role userRole;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class updateResponse {
        @Schema(description = "변경된 이메일", example = "newemail@example.com")
        private String email;
        @Schema(description = "변경된 유저 이름", example = "김코딩")
        private String userName;
        @Schema(description = "변경된 닉네임", example = "자바의 신")
        private String userNickname;
        @Schema(description = "변경된 프로필 이미지 URL", example = "https://example.com/images/profile/abcd.jpg")
        private String userProfileImage;
    }
}
