package com.example.backend.schedule.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    // 💡 1. ScheduleService에서 사용할 수 있도록 public record로 DTO를 정의합니다.
    public record ItemWithLocationInfo(String contentId, String title, double latitude, double longitude) {}

    // 💡 2. 메서드의 파라미터를 `List<ScheduleItem>`에서 `List<ItemWithLocationInfo>`로 변경합니다.
    public Mono<String> getOptimizedRouteJson(UUID scheduleId, LocalDate startDate, LocalDate endDate, List<ItemWithLocationInfo> itemsWithLocation) {
        log.info("🚀 AI 경로 최적화 시작 - Schedule ID: {}", scheduleId);

        String prompt = createOptimizationPrompt(scheduleId, startDate, endDate, itemsWithLocation);
        log.debug("🤖 생성된 프롬프트: \n{}", prompt);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClient.post()
                .uri("https://api.openai.com/v1/chat/completions")
                .headers(h -> h.addAll(headers))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnNext(response -> {
                    try {
                        log.debug("⬅️ OpenAI API 원본 응답: {}", objectMapper.writeValueAsString(response));
                    } catch (JsonProcessingException e) {
                        log.warn("API 응답 JSON 변환 실패 (로깅 목적)", e);
                    }
                })
                .map(this::extractContentFromApiResponse)
                .doOnSuccess(content -> log.info("✅ AI 경로 최적화 응답 처리 성공 - Schedule ID: {}", scheduleId))
                .onErrorMap(throwable -> {
                    log.error("❌ OpenAI API 호출 중 심각한 오류 발생 - Schedule ID: {}", scheduleId, throwable);
                    return new RuntimeException("OpenAI API 호출 실패: " + throwable.getMessage(), throwable);
                });
    }

    // 💡 3. 프롬프트 생성 메서드도 변경된 파라미터를 받도록 수정합니다.
    private String createOptimizationPrompt(UUID scheduleId, LocalDate startDate, LocalDate endDate, List<ItemWithLocationInfo> items) {
        log.info("프롬프트 생성을 시작합니다...");

        String itemsJson;
        try {
            // 이제 items 리스트에 이미 위도, 경도가 포함되어 있으므로 바로 JSON으로 변환합니다.
            itemsJson = objectMapper.writeValueAsString(items);
            log.debug("직렬화된 스케줄 아이템 JSON: {}", itemsJson);
        } catch (JsonProcessingException e) {
            log.error("스케줄 아이템 JSON 직렬화 실패", e);
            throw new RuntimeException("JSON 직렬화에 실패했습니다.", e);
        }

        return String.format("""
            너는 여행 일정 최적화 및 JSON 변환 전문가야.
            아래의 여행 기간과 장소 목록을 참고하여 최적의 여행 일정을 구성하고, 지정된 JSON 형식으로만 반환해줘.
            **작업 규칙:**
            1.  **역할 및 목적:** 주어진 장소들을 가장 효율적인 동선으로 정렬하고, 각 장소에 적절한 방문 시간과 일자를 할당하는 것이 목표야.
            2.  **숙소 처리:** `is_lodging`이 `true`인 항목은 해당 날짜 일정의 마지막 장소로 배치해야 해.
            3.  **시간 할당:**
                * **방문 시간:** 각 장소당 평균 방문 시간을 2시간으로 할당해줘.
                * **이동 시간:** 위도와 경도를 참고하여, 장소 간 이동 시간을 30분으로 할당해줘.
                * **시작 시간:** 첫째 날의 일정은 오전 10:00에 시작하는 것으로 설정해줘.
            4.  **일정 배분:** 각 날짜(dayNumber)에 할당되는 아이템의 개수가 최대한 균등하도록 배분해줘.
            5.  **최적화:** 위도와 경도를 기준으로 지리적으로 가까운 장소들을 묶어서 효율적인 동선을 만들어줘.
            6.  **JSON 형식:** 아래에 제시된 JSON 구조를 정확하게 따르고, 다른 설명이나 텍스트는 일체 포함하지 마.
            
            **입력 정보:**
            * 여행 기간: %s 부터 %s 까지
            * 스케줄 ID: %s
            * 스케줄 아이템 목록:
            %s
            
            **JSON 출력 형식:**
            {
              "scheduleId": "%s",
              "ScheduleItems": [
                 {
                   "order": 1,
                   "contentId": "장소의 content_id",
                   "dayNumber": 1,
                   "start_time": "10:00",
                   "end_time": "12:00"
                 }
              ]
            }
            
            **참고:** 만약 `is_lodging` 필드가 없다면, '숙소'는 없다고 간주하고 모든 장소를 최적화해줘.
            """,
                startDate,
                endDate,
                scheduleId,
                itemsJson,
                scheduleId
        );
    }

    private String extractContentFromApiResponse(Map<String, Object> apiResponse) {
        log.info("API 응답에서 content 추출을 시작합니다...");
        List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
        if (choices == null || choices.isEmpty()) {
            log.error("API 응답 오류: 'choices' 필드가 없거나 비어있습니다. 응답: {}", apiResponse);
            throw new RuntimeException("OpenAI 응답에 'choices'가 없습니다.");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            log.error("API 응답 오류: 'message' 필드가 없습니다. 응답: {}", apiResponse);
            throw new RuntimeException("OpenAI 응답에 'message'가 없습니다.");
        }
        String content = (String) message.get("content");
        if (content == null || content.isBlank()) {
            log.error("API 응답 오류: 'content' 필드가 비어있습니다. 응답: {}", apiResponse);
            throw new RuntimeException("OpenAI 응답에 'content'가 비어있습니다.");
        }
        log.debug("추출된 content: {}", content);
        log.info("content 추출을 완료했습니다.");
        return content;
    }
}