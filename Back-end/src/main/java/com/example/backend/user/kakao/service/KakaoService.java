package com.example.backend.user.kakao.service;

import com.example.backend.jwt.config.JWTGenerator;
import com.example.backend.jwt.dto.JwtDto;
import com.example.backend.user.kakao.dto.response.KakaoResponse;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoService {

    private final UserRepository userRepository;
    private final JWTGenerator jwtGenerator;

    @Value("${kakao.restApiKey}")
    private String restApiKey;

    // 웹 시작용 리디렉션 (GET /auth/kakao/login 에서만 사용)
    @Value("${kakao.redirectUri}")
    private String redirectUri;

    /**
     * (웹 전용) 카카오 로그인 시작 URL 구성
     * 앱에서는 사용하지 않습니다.
     */
    public String getKakaoAuthUrl() {
        return "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + restApiKey
                + "&redirect_uri=" + redirectUri
                + "&scope=account_email,profile_nickname,profile_image";
    }

    /**
     * ✅ 콜백 처리: 프론트에서 전달한 redirectUri(앱/웹)를 그대로 사용해 토큰 교환
     */
    public ResponseEntity<KakaoResponse.loginResponse> getUserInfo(String code, String redirectUriFromClient) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // --- 1) 토큰 교환 ---
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", restApiKey);
            // ✅ 핵심: 고정 redirectUri 대신, 프론트에서 온 redirectUri 사용
            params.add("redirect_uri", redirectUriFromClient);
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token", tokenRequest, Map.class
            );

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // --- 2) 사용자 정보 조회 ---
            HttpHeaders userInfoHeaders = new HttpHeaders();
            userInfoHeaders.set("Authorization", "Bearer " + accessToken);
            HttpEntity<Void> userInfoRequest = new HttpEntity<>(userInfoHeaders);

            ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
                    "https://kapi.kakao.com/v2/user/me",
                    HttpMethod.GET,
                    userInfoRequest,
                    Map.class
            );

            Map<String, Object> body = userInfoResponse.getBody();
            Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String email = (String) kakaoAccount.get("email");
            String nickname = (String) profile.get("nickname");
            String profileImage = (String) profile.get("profile_image_url");

            // --- 3) 유저 upsert ---
            User user;
            if (userRepository.existsByEmail(email)) {
                user = userRepository.findByEmail(email).orElseThrow();
            } else {
                user = User.builder()
                        .email(email)
                        .userName(nickname)
                        .userNickname(nickname)
                        .userProfileImage(profileImage)
                        .userRole(User.Role.USER)
                        .build();
                userRepository.save(user);
            }

            // --- 4) JWT 발급 ---
            JwtDto jwtDto = jwtGenerator.generateToken(user);

            // --- 5) 응답 ---
            return ResponseEntity.ok(
                    KakaoResponse.loginResponse.builder()
                            .jwtDto(jwtDto)
                            .userNickname(user.getUserNickname())
                            .userProfileImage(user.getUserProfileImage())
                            .userEmail(user.getEmail())
                            .userName(user.getUserName())
                            .build()
            );

        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}