package com.example.backend.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health/redis")
@Tag(name = "RedisTestAPI", description = "Redis 설정 테스트 API")
public class RedisTestController {

    private final RedisTemplate<String, String> redisTemplate;

    @PostMapping("/set")
    @Operation(summary = "Redis 값 저장", description = "Redis에 키-값 쌍을 저장합니다")
    public ResponseEntity<Map<String, Object>> setRedisValue(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam(defaultValue = "300") long ttlSeconds) {

        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Redis에 값이 저장되었습니다");
        response.put("key", key);
        response.put("value", value);
        response.put("ttl", ttlSeconds + " seconds");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get")
    @Operation(summary = "Redis 값 조회", description = "Redis에서 키로 값을 조회합니다")
    public ResponseEntity<Map<String, Object>> getRedisValue(@RequestParam String key) {
        String value = redisTemplate.opsForValue().get(key);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("value", value);
        response.put("exists", value != null);
        response.put("ttl", ttl + " seconds");
        response.put("timestamp", System.currentTimeMillis());

        if (value == null) {
            response.put("message", "키가 존재하지 않거나 만료되었습니다");
        } else {
            response.put("message", "값 조회 성공");
        }

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "Redis 값 삭제", description = "Redis에서 키를 삭제합니다")
    public ResponseEntity<Map<String, Object>> deleteRedisValue(@RequestParam String key) {
        Boolean deleted = redisTemplate.delete(key);

        Map<String, Object> response = new HashMap<>();
        response.put("key", key);
        response.put("deleted", deleted);
        response.put("message", deleted ? "키가 삭제되었습니다" : "키가 존재하지 않습니다");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/connection-test")
    @Operation(summary = "Redis 연결 테스트", description = "Redis 서버 연결 상태를 확인합니다")
    public ResponseEntity<Map<String, Object>> testRedisConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 간단한 ping 테스트
            redisTemplate.opsForValue().set("connection-test", "ok", 10, TimeUnit.SECONDS);
            String result = redisTemplate.opsForValue().get("connection-test");
            redisTemplate.delete("connection-test");

            response.put("connected", "ok".equals(result));
            response.put("message", "Redis 연결 성공");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("connected", false);
            response.put("message", "Redis 연결 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/info")
    @Operation(summary = "Redis 정보 조회", description = "Redis Template 설정 정보를 조회합니다")
    public ResponseEntity<Map<String, Object>> getRedisInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("keySerializer", redisTemplate.getKeySerializer().getClass().getSimpleName());
        response.put("valueSerializer", redisTemplate.getValueSerializer().getClass().getSimpleName());
        response.put("connectionFactory", redisTemplate.getConnectionFactory().getClass().getSimpleName());
        response.put("message", "Redis Template 정보 조회 성공");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}