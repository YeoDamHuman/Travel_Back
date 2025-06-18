package com.example.backend.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AsyncTestService {

    @Async
    public void asyncTask() {
        log.info("🔥 asyncTask 실행됨! Thread: {}", Thread.currentThread().getName());

        try {
            Thread.sleep(2000); // 일부러 2초 기다림
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("✅ asyncTask 끝남! Thread: {}", Thread.currentThread().getName());
    }
}