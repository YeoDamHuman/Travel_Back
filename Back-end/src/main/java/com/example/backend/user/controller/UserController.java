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
    public ResponseEntity<Void> register(@RequestBody UserRequest.registerRequest request) {
        userService.register(request);
        return ResponseEntity.status(201).build(); // 201 Created
    }

    @PostMapping("/login")
    @Operation(summary = "기본 유저 로그인", description = "카카오톡 로그인이 아닌 로컬 로그인 API")
    public ResponseEntity<UserResponse.loginResponse> login(@RequestBody UserRequest.loginRequest login) {
        UserResponse.loginResponse response = userService.login(login);
        return ResponseEntity.ok(response);
    }

}
