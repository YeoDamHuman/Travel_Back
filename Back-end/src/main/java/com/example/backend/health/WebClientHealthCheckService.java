package com.example.backend.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebClientHealthCheckService {

    private final WebClient webClient;

    public String testGet() {
        try {
            log.info("WebClient 테스트 시작 - httpbin.org/get 호출");

            String result = webClient.get()
                    .uri("/get")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10)) // 10초 타임아웃 설정
                    .block();

            log.info("WebClient 테스트 성공");
            return result;

        } catch (WebClientException e) {
            log.error("WebClient 오류 발생: {}", e.getMessage(), e);
            return "WebClient 오류: " + e.getMessage();
        } catch (Exception e) {
            log.error("예상하지 못한 오류 발생: {}", e.getMessage(), e);
            return "예상하지 못한 오류: " + e.getMessage();
        }
    }

    // 간단한 테스트용 메서드 추가
    public String simpleTest() {
        return "WebClient 서비스가 정상적으로 작동합니다.";
    }
}