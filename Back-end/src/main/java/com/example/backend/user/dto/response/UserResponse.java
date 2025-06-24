package com.example.backend.user.dto.response;

import com.example.backend.jwt.dto.JwtDto;
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
    }
}
