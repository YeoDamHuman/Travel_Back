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

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    public record ItemWithLocationInfo(String contentId, String title, double latitude, double longitude, String category) {}

    public Mono<String> getOptimizedRouteJson(UUID scheduleId, LocalDate startDate, LocalDate endDate, List<ItemWithLocationInfo> itemsWithLocation) {
        log.info("🚀 AI 경로 최적화 시작 - Schedule ID: {}", scheduleId);

        String prompt = createOptimizationPrompt(scheduleId, startDate, endDate, itemsWithLocation);
        log.debug("🤖 생성된 프롬프트: \n{}", prompt);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
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

    private String createOptimizationPrompt(UUID scheduleId, LocalDate startDate, LocalDate endDate, List<ItemWithLocationInfo> items) {
        log.info("프롬프트 생성을 시작합니다...");

        String itemsJson;
        try {
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
            1.  **사고 과정:** 너의 작업을 **Step-by-step**으로 신중하게 생각하고, 모든 규칙을 준수한 후에 최종 JSON을 출력해줘.
            2.  **역할 및 목적:** 주어진 장소들을 가장 효율적인 동선으로 정렬하고, 각 장소에 적절한 방문 일자를 할당하는 것이 목표야.
            3.  **시간 할당 (내부 계산용):**
                * **방문 시간:** 각 장소당 평균 방문 시간을 2시간으로 할당.
                * **이동 시간:** 위도와 경도를 참고하여, 장소 간 이동 시간을 30분으로 할당.
                * **시작 시간:** 첫째 날의 일정은 오전 10:00에 시작하는 것으로 가정.
                * 이 시간 규칙들은 최적의 순서와 날짜 배분을 위해 **너의 내부 계산에만 사용**하고, 최종 JSON 결과에는 포함하지 마.
            4.  **일정 배분:** 각 날짜(dayNumber)에 할당되는 아이템의 개수가 최대한 균등하도록 배분해줘.
            5.  **최적화:** 너는 모든 장소를 방문하는 가장 효율적인 경로를 찾아야 해. 이는 **다익스트라(Dijkstra) 알고리즘**이나 **최단 경로 찾기(Shortest Path Finding)**와 유사한 접근 방식을 사용하여, 위도와 경도 데이터를 기반으로 전체 이동 거리를 최소화하는 것을 의미해.
            6.  **JSON 형식:** 아래에 제시된 JSON 구조를 정확하게 따르고, 다른 설명이나 텍스트는 일체 포함하지 마. 반드시 모든 규칙과 최적화 과정을 거친 후에 이 형식에 맞춰 출력해야 해.
            7.  **카테고리 고려:** 스케줄 아이템의 `category` 정보를 바탕으로 일정을 지능적으로 구성해줘. (예: 'RESTAURANT'는 점심/저녁 시간대에, 'ACCOMMODATION'은 하루 일정의 마지막에, 'TOURIST_SPOT'이나 'LEISURE', 'HEALING' 등 나머지 활동은 그 사이에 효율적으로 배치)

            **입력 정보:**
            * 여행 기간: %s 부터 %s 까지
            * 스케줄 ID: %s
            * 스케줄 아이템 목록 (이제 category 포함):
            %s

            **JSON 출력 형식:**
            {
              "scheduleId": "%s",
              "ScheduleItems": [
                 {
                   "order": 1,
                   "contentId": "장소의 content_id",
                   "dayNumber": 1
                 }
              ]
            }
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