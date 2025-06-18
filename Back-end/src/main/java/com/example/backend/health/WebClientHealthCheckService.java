package com.example.backend.health;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WebClientHealthCheckService {

    private final WebClient webClient;

    public String testGet() {
        return webClient.get()
                .uri("/get") // baseUrl이 httpbin이면 /get 만 써도 OK
                .retrieve()
                .bodyToMono(String.class)
                .block(); // 동기 처리
    }
}

