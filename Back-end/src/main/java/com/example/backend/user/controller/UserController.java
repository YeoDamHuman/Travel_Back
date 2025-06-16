package com.example.backend.user.controller;

import com.example.backend.jwt.dto.JwtDto;
import com.example.backend.user.dto.UserRequest;
import com.example.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequest.loginRequest login) {
        try {
            JwtDto jwtDto = userService.login(login);  // 토큰 생성된 JwtDto 받음
            return ResponseEntity.ok(jwtDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRequest.registerRequest request) {
        try {
            userService.register(request); // 회원가입 처리
            return ResponseEntity.ok("회원가입 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
