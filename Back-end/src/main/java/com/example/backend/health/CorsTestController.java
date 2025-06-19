package com.example.backend.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health/cors")
@Tag(name = "CorsTestAPI", description = "CORS 설정 테스트 API")
public class CorsTestController {

    @GetMapping("/test")
    @Operation(summary = "CORS GET 테스트", description = "CORS 설정이 올바른지 GET 요청으로 테스트")
    public ResponseEntity<Map<String, Object>> testCorsGet() {
        Map<String, Object> response = new HashMap<>();
        response.put("method", "GET");
        response.put("message", "CORS GET 요청 성공!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test")
    @Operation(summary = "CORS POST 테스트", description = "CORS 설정이 올바른지 POST 요청으로 테스트")
    public ResponseEntity<Map<String, Object>> testCorsPost(@RequestBody(required = false) Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        response.put("method", "POST");
        response.put("message", "CORS POST 요청 성공!");
        response.put("receivedData", requestBody);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/test")
    @Operation(summary = "CORS PUT 테스트", description = "CORS 설정이 올바른지 PUT 요청으로 테스트")
    public ResponseEntity<Map<String, Object>> testCorsPut(@RequestBody(required = false) Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        response.put("method", "PUT");
        response.put("message", "CORS PUT 요청 성공!");
        response.put("receivedData", requestBody);
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/test")
    @Operation(summary = "CORS DELETE 테스트", description = "CORS 설정이 올바른지 DELETE 요청으로 테스트")
    public ResponseEntity<Map<String, Object>> testCorsDelete() {
        Map<String, Object> response = new HashMap<>();
        response.put("method", "DELETE");
        response.put("message", "CORS DELETE 요청 성공!");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @RequestMapping(value = "/test", method = RequestMethod.OPTIONS)
    @Operation(summary = "CORS OPTIONS 테스트", description = "CORS Preflight 요청 테스트")
    public ResponseEntity<Map<String, Object>> testCorsOptions() {
        Map<String, Object> response = new HashMap<>();
        response.put("method", "OPTIONS");
        response.put("message", "CORS OPTIONS 요청 성공!");
        response.put("allowedMethods", "GET, POST, PUT, DELETE, OPTIONS");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}