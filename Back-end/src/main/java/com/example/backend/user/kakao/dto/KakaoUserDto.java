package com.example.backend.user.kakao.dto;

import lombok.Data;

@Data
public class KakaoUserDto {
    private String email;
    private String name;
    private String nickname;
    private String profileImage;
}