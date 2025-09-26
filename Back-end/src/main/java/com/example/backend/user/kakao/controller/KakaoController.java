package com.example.backend.user.kakao.controller;

import com.example.backend.user.kakao.dto.request.KakaoRequest;
import com.example.backend.user.kakao.dto.response.KakaoResponse;
import com.example.backend.user.kakao.service.KakaoService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/auth/kakao")
@RequiredArgsConstructor
public class KakaoController {

    private final KakaoService kakaoService;

    // 로그인 페이지 리디렉션
    @GetMapping("/login")
    public void redirectToKakao(HttpServletResponse response) throws IOException {
        String kakaoAuthUrl = kakaoService.getKakaoAuthUrl();
        response.sendRedirect(kakaoAuthUrl);
    }

    // 카카오 콜백 (백엔드 직접 받음)
    @GetMapping("/callback")
    public ResponseEntity<KakaoResponse.loginResponse> kakaoCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state
    ) {
        return kakaoService.getUserInfo(code);
    }
}
