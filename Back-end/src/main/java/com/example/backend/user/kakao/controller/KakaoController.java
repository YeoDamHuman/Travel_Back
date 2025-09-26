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

    @Operation(
            summary = "카카오 로그인 페이지 리디렉션",
            description = "카카오 로그인 페이지로 리디렉션합니다. 프론트는 이 URL로 GET 요청하세요."
    )
    @GetMapping("/login")
    public void redirectToKakao(
            @RequestParam(value = "state", required = false) String state,
            HttpServletResponse response
    ) throws IOException {
        String kakaoAuthUrl = kakaoService.getKakaoAuthUrl(state);
        response.sendRedirect(kakaoAuthUrl);
    }


    @Operation(
            summary = "카카오 로그인 콜백",
            description = "카카오 로그인 완료 후 받은 code를 이용해 JWT 및 유저 정보를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT 및 유저 정보 반환 성공"),
    })
    @PostMapping("/callback")
    public ResponseEntity<KakaoResponse.loginResponse> kakaoCallback(
            @RequestBody KakaoRequest request
    ) {
        return kakaoService.getUserInfo(request.getCode());
    }


}