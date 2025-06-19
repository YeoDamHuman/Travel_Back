package com.example.backend.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health")
@Slf4j
public class HealthCheckController {

    private final WebClientHealthCheckService service;
    private final AsyncTestService asyncTestService;

    @GetMapping("/webclient")
    public ResponseEntity<String> checkWebClient() {
        try {
            log.info("WebClient 헬스체크 요청 받음");
            String response = service.testGet();
            log.info("WebClient 헬스체크 응답: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("WebClient 헬스체크 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("WebClient 테스트 실패: " + e.getMessage());
        }
    }

    @GetMapping("/async")
    public ResponseEntity<String> checkAsync() {
        try {
            log.info("Async 헬스체크 요청 받음");
            asyncTestService.asyncTask(); // 비동기 호출
            return ResponseEntity.ok("Async 호출됨! 로그 확인해보세요.");
        } catch (Exception e) {
            log.error("Async 헬스체크 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("Async 테스트 실패: " + e.getMessage());
        }
    }

    // 기본 헬스체크 엔드포인트 추가
    @GetMapping("/status")
    public ResponseEntity<String> checkStatus() {
        return ResponseEntity.ok("서버가 정상적으로 작동중입니다.");
    }

    // WebClient 간단 테스트
    @GetMapping("/webclient/simple")
    public ResponseEntity<String> checkWebClientSimple() {
        try {
            String response = service.simpleTest();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("WebClient 간단 테스트 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("WebClient 간단 테스트 실패: " + e.getMessage());
        }
    }
}