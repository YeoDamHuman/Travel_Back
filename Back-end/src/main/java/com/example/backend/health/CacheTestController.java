package com.example.backend.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health/cache")
@Tag(name = "CacheTestAPI", description = "캐시 설정 테스트 API")
public class CacheTestController {

    @GetMapping("/tours/{tourId}")
    @Operation(summary = "투어 캐시 테스트", description = "투어 정보를 캐시에서 조회 (10분간 캐시)")
    @Cacheable(value = "tours", key = "#tourId")
    public ResponseEntity<Map<String, Object>> getTourWithCache(@PathVariable String tourId) {
        // 실제로는 DB에서 조회하지만, 테스트용으로 시뮬레이션
        Map<String, Object> tourInfo = new HashMap<>();
        tourInfo.put("tourId", tourId);
        tourInfo.put("name", "캐시 테스트 투어");
        tourInfo.put("location", "서울");
        tourInfo.put("createdAt", LocalDateTime.now()); // 캐시 확인용
        tourInfo.put("message", "이 시간이 같으면 캐시에서 조회된 것입니다!");

        return ResponseEntity.ok(tourInfo);
    }

    @GetMapping("/weather/{city}")
    @Operation(summary = "날씨 캐시 테스트", description = "날씨 정보를 캐시에서 조회 (10분간 캐시)")
    @Cacheable(value = "weather", key = "#city")
    public ResponseEntity<Map<String, Object>> getWeatherWithCache(@PathVariable String city) {
        Map<String, Object> weatherInfo = new HashMap<>();
        weatherInfo.put("city", city);
        weatherInfo.put("temperature", "22°C");
        weatherInfo.put("condition", "맑음");
        weatherInfo.put("createdAt", LocalDateTime.now()); // 캐시 확인용
        weatherInfo.put("message", "이 시간이 같으면 캐시에서 조회된 것입니다!");

        return ResponseEntity.ok(weatherInfo);
    }

    @DeleteMapping("/clear")
    @Operation(summary = "캐시 전체 삭제", description = "모든 캐시를 삭제합니다")
    public ResponseEntity<String> clearCache() {
        // 실제 구현시에는 CacheManager를 주입받아 사용
        return ResponseEntity.ok("캐시가 삭제되었습니다. (실제로는 CacheManager 구현 필요)");
    }
}