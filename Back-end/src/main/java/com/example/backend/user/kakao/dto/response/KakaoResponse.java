package com.example.backend.user.kakao.dto.response;

import com.example.backend.jwt.dto.JwtDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class KakaoResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "카카오 로그인 응답 DTO")
    public static class loginResponse {

        @Schema(description = "JWT 토큰 (access & refresh)", example = "{accessToken: '...', refreshToken: '...'}")
        private JwtDto jwtDto;

        @Schema(description = "유저 닉네임", example = "도적")
        private String userNickname;

        @Schema(description = "유저 프로필 이미지 URL", example = "https://k.kakaocdn.net/.../profile.jpg")
        private String userProfileImage;

        @Schema(description = "사용자 이메일", example = "HONG@example.com")
        private String userEmail;

    }
}