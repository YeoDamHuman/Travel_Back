package com.example.backend.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 반환")
public record JwtDto(@Schema(description = "AccessToken", example = "asgsasdgasdg")String accessToken,
                     @Schema(description = "RefreshToken", example = "asgfkjasldg")String refreshToken) {
}