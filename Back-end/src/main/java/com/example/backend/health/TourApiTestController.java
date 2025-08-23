package com.example.backend.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/health/tour-api")
@Tag(name = "TourAPI 테스트", description = "한국관광공사 API 연동 테스트")
@Slf4j
public class TourApiTestController {

    private final WebClient webClient;

    @Value("${tour.api.key}")
    private String apiKey;

    @Value("${tour.api.base-url:http://apis.data.go.kr/B551011/KorService2}")
    private String baseUrl;

    @GetMapping("/basic-test")
    @Operation(summary = "기본 API 연결 테스트", description = "한국관광공사 API 기본 연결 상태 확인")
    public ResponseEntity<Map<String, Object>> basicApiTest() {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("=== TourAPI 기본 테스트 시작 ===");
            log.info("API Key: {}", apiKey);
            log.info("Base URL: {}", baseUrl);

            // 올바른 API 엔드포인트 사용
            String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/areaBasedList2")
                    .queryParam("serviceKey", URLEncoder.encode(apiKey, StandardCharsets.UTF_8))
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "TravelPlanner")
                    .queryParam("_type", "json")
                    .queryParam("arrange", "A")
                    .queryParam("pageNo", "1")
                    .queryParam("numOfRows", "5")
                    .queryParam("areaCode", "1")  // 서울
                    .build()
                    .toUriString();

            log.info("요청 URL: {}", uri);

            String apiResponse = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("API 응답 (처음 200자): {}", apiResponse.substring(0, Math.min(200, apiResponse.length())));

            response.put("success", true);
            response.put("message", "API 호출 성공");
            response.put("requestUrl", uri);
            response.put("responsePreview", apiResponse.substring(0, Math.min(500, apiResponse.length())));
            response.put("responseLength", apiResponse.length());
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("TourAPI 기본 테스트 실패", e);
            response.put("success", false);
            response.put("message", "API 호출 실패: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/search-test")
    @Operation(summary = "키워드 검색 API 테스트", description = "한국관광공사 키워드 검색 API 테스트")
    public ResponseEntity<Map<String, Object>> searchApiTest(
            @Parameter(description = "검색 키워드", example = "서울")
            @RequestParam(value = "keyword", defaultValue = "서울") String keyword) {

        Map<String, Object> response = new HashMap<>();

        try {
            log.info("=== TourAPI 키워드 검색 테스트 시작 ===");
            log.info("검색 키워드: {}", keyword);

            // 직접 URL 문자열 생성
            String uri = String.format(
                    "%s/searchKeyword2?serviceKey=%s&MobileOS=ETC&MobileApp=TravelPlanner&_type=json&arrange=A&pageNo=1&numOfRows=5&keyword=%s",
                    baseUrl,
                    apiKey,
                    URLEncoder.encode(keyword, StandardCharsets.UTF_8)
            );

            log.info("요청 URL: {}", uri);

            // 완성된 URI 직접 사용
            String apiResponse = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("API 응답: {}", apiResponse);

            response.put("success", true);
            response.put("message", "키워드 검색 API 호출 성공");
            response.put("keyword", keyword);
            response.put("requestUrl", uri);
            response.put("response", apiResponse);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("TourAPI 키워드 검색 테스트 실패", e);
            response.put("success", false);
            response.put("message", "키워드 검색 API 호출 실패: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/raw-response")
    @Operation(summary = "원본 응답 확인", description = "API 원본 응답을 그대로 반환 (디버깅용)")
    public ResponseEntity<String> getRawResponse(
            @Parameter(description = "검색 키워드", example = "서울")
            @RequestParam(value = "keyword", defaultValue = "서울") String keyword) {

        try {
            // API 키 로그 확인
            log.info("현재 API 키: {}", apiKey);

            // 직접 URL 문자열로 생성 (listYN 제거)
            String uri = baseUrl + "/searchKeyword2" +
                    "?serviceKey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8) +
                    "&MobileOS=ETC" +
                    "&MobileApp=TravelPlanner" +
                    "&_type=json" +
                    "&arrange=A" +
                    "&pageNo=1" +
                    "&numOfRows=10" +
                    "&keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);

            log.info("원본 응답 요청 URL: {}", uri);

            String apiResponse = webClient.get()
                    .uri(uri)  // 문자열 URI 직접 사용
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return ResponseEntity.ok()
                    .header("Content-Type", "application/json; charset=utf-8")
                    .body(apiResponse);

        } catch (Exception e) {
            log.error("원본 응답 조회 실패", e);
            return ResponseEntity.status(500)
                    .body("API 호출 실패: " + e.getMessage());
        }
    }

    @GetMapping("/config-check")
    @Operation(summary = "설정 정보 확인", description = "현재 TourAPI 설정 정보 확인")
    public ResponseEntity<Map<String, Object>> checkConfig() {
        Map<String, Object> response = new HashMap<>();

        response.put("apiKey", apiKey != null ? "설정됨 (길이: " + apiKey.length() + ")" : "설정되지 않음");
        response.put("baseUrl", baseUrl);
        response.put("webClientConfigured", webClient != null);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/simple-call")
    @Operation(summary = "단순 API 호출", description = "파라미터 없이 단순한 API 호출")
    public ResponseEntity<Map<String, Object>> simpleCall() {
        Map<String, Object> response = new HashMap<>();

        try {
            // API 키를 디코딩하지 않고 그대로 사용
            String uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                    .path("/areaBasedList2")
                    .queryParam("serviceKey", URLEncoder.encode(apiKey, StandardCharsets.UTF_8))
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "TravelPlanner")
                    .queryParam("_type", "json")
                    .queryParam("arrange", "A")
                    .queryParam("numOfRows", "5")
                    .queryParam("pageNo", "1")
                    .queryParam("areaCode", "1")
                    .build()
                    .toUriString();

            log.info("단순 호출 URL: {}", uri);

            String apiResponse = webClient.get()
                    .uri(uri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            response.put("success", true);
            response.put("requestUrl", uri);
            response.put("responsePreview", apiResponse.substring(0, Math.min(300, apiResponse.length())));
            response.put("isJson", apiResponse.trim().startsWith("{") || apiResponse.trim().startsWith("["));
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("단순 API 호출 실패", e);
            response.put("success", false);
            response.put("message", "API 호출 실패: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(response);
        }
    }
}