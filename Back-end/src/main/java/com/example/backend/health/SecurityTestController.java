package com.example.backend.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health/security")
@Tag(name = "SecurityTestAPI", description = "보안 설정 테스트 API")
public class SecurityTestController {

    @GetMapping("/public")
    @Operation(summary = "공개 엔드포인트 테스트", description = "인증 없이 접근 가능한 엔드포인트")
    public ResponseEntity<Map<String, Object>> testPublicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "공개 엔드포인트 접근 성공!");
        response.put("authentication", "인증 불필요");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/protected")
    @Operation(summary = "보호된 엔드포인트 테스트",
            description = "JWT 토큰이 필요한 보호된 엔드포인트",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Map<String, Object>> testProtectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "보호된 엔드포인트 접근 성공!");
        response.put("username", authentication != null ? authentication.getName() : "Unknown");
        response.put("authorities", authentication != null ? authentication.getAuthorities() : "None");
        response.put("authenticated", authentication != null ? authentication.isAuthenticated() : false);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/protected")
    @Operation(summary = "보호된 POST 테스트",
            description = "JWT 토큰이 필요한 POST 요청 테스트",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Map<String, Object>> testProtectedPost(@RequestBody(required = false) Map<String, Object> requestBody) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "보호된 POST 요청 성공!");
        response.put("username", authentication != null ? authentication.getName() : "Unknown");
        response.put("receivedData", requestBody);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-info")
    @Operation(summary = "사용자 정보 조회",
            description = "현재 인증된 사용자의 정보를 조회",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Map<String, Object>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> response = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("principal", authentication.getPrincipal().toString());
            response.put("name", authentication.getName());
            response.put("authorities", authentication.getAuthorities());
            response.put("details", authentication.getDetails());
            response.put("authenticated", true);
        } else {
            response.put("message", "인증되지 않은 사용자");
            response.put("authenticated", false);
        }
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}