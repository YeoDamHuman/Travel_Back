package com.example.backend.user.controller;

import com.example.backend.jwt.dto.JwtDto;
import com.example.backend.user.dto.request.UserRequest;
import com.example.backend.user.dto.response.UserResponse;
import com.example.backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "UserAPI", description = "User관련 데이터 불러오는 API")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "기본 유저 회원가입", description = "로컬 회원가입 API")
    public ResponseEntity<UserResponse.registerResponse> register(@RequestBody UserRequest.registerRequest request) {
        try {
//            UUID uuid = userService.register(request);  // 이제 UUID 반환한다고 가정
            userService.register(request);
            UserResponse.registerResponse response = UserResponse.registerResponse.builder()
                    .message("회원가입이 완료되었습니다.")
                    .build();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(UserResponse.registerResponse
                            .builder()
                            .message(e.getMessage())
                            .build());
        }
    }

    @PostMapping("/login")
    @Operation(summary = "기본 유저 로그인", description = "카카오톡 로그인이 아닌 로컬 로그인 API")
    public ResponseEntity<JwtDto> login(@RequestBody UserRequest.loginRequest login) {
        try {
            JwtDto jwtDto = userService.login(login);  // 토큰 생성된 JwtDto 받음
            return ResponseEntity.ok(jwtDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }



}
