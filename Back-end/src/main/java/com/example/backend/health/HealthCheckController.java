package com.example.backend.health;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
public class HealthCheckController {

    private final WebClientHealthCheckService service;
    private final AsyncTestService asyncTestService;

    @GetMapping("/webclient")
    public ResponseEntity<String> checkWebClient() {
        String response = service.testGet();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/async")
    public ResponseEntity<String> checkAsync() {
        asyncTestService.asyncTask(); // 비동기 호출
        return ResponseEntity.ok("Async 호출됨! 로그 확인해보세요.");
    }
}