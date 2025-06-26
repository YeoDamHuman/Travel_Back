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

    @Value("${kakao.redirectUri}")
    private String redirectUri;

    public String getKakaoAuthUrl() {
        return "https://kauth.kakao.com/oauth/authorize?response_type=code"
                + "&client_id=" + restApiKey
                + "&redirect_uri=" + redirectUri
                + "&scope=account_email,profile_nickname,profile_image";
    }

    public ResponseEntity<KakaoResponse.loginResponse> getUserInfo(String code) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            // access_token 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", restApiKey);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                    "https://kauth.kakao.com/oauth/token", tokenRequest, Map.class);

            String accessToken = (String) tokenResponse.getBody().get("access_token");

            // 사용자 정보 요청
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

            // 유저 저장 또는 가져오기
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

            // JWT 생성
            JwtDto jwtDto = jwtGenerator.generateToken(user);

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
                    .body(null); // 또는 에러 응답 DTO를 정의해도 됨
        }
    }
}