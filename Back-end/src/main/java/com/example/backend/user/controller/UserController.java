package com.example.backend.user.controller;

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

    @PutMapping("/update")
    @Operation(summary = "유저 정보 수정", description = "로컬 유저 정보 수정 API")
    public ResponseEntity<UserResponse.updateResponse> update(@RequestBody UserRequest.updateRequest request) {
        UserResponse.updateResponse response = userService.update(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "유저 삭제", description = "로컬 유저 정보 수정 API")
    public ResponseEntity<Void> delete() {
        userService.delete();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "JWT 토큰 기반으로 현재 로그인한 유저 정보 반환")
    public ResponseEntity<UserResponse.InformationResponse> myinfo() {
        UserResponse.InformationResponse response = userService.myInfo();
        return ResponseEntity.ok(response);
    }


    @PostMapping("/login")
    @Operation(summary = "기본 유저 로그인", description = "카카오톡 로그인이 아닌 로컬 로그인 API")
    public ResponseEntity<UserResponse.loginResponse> login(@RequestBody UserRequest.loginRequest login) {
        UserResponse.loginResponse response = userService.login(login);
        return ResponseEntity.ok(response);
    }

}
