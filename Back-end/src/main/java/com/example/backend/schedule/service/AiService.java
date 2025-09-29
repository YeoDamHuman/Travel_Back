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
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
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

    /**
     * AI를 이용해 주어진 장소 목록을 날짜별로 균등 배분한 '중간 계획 JSON'을 생성합니다.
     *
     * @return 날짜별로 장소가 배정된 JSON 문자열
     */
    public Mono<String> createDailyPlanJson(UUID scheduleId, LocalDate startDate, LocalDate endDate, LocalTime startTime, List<ItemWithLocationInfo> itemsWithLocation) {
        log.info("🚀 AI 일정 배분 시작 - Schedule ID: {}", scheduleId);

        String prompt = PromptFactory.createDailyPlanPrompt(scheduleId, startDate, endDate, startTime, itemsWithLocation);

        return callOpenAiApi(prompt)
                .doOnSuccess(dailyPlanJson -> {
                    log.info("✅ AI 일정 배분 성공! - Schedule ID: {}", scheduleId);
                    log.debug("📄 생성된 일자별 계획 JSON: {}", dailyPlanJson);
                })
                .onErrorMap(throwable -> {
                    log.error("❌ OpenAI API 호출 중 오류 발생 - Schedule ID: {}", scheduleId, throwable);
                    return new RuntimeException("OpenAI API 호출 실패: " + throwable.getMessage(), throwable);
                });
    }

    /**
     * OpenAI API 호출
     */
    private Mono<String> callOpenAiApi(String prompt) {
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
                .map(this::extractContentFromApiResponse);
    }

    private String extractContentFromApiResponse(Map<String, Object> apiResponse) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("OpenAI 응답에 'choices'가 없습니다. 응답: " + apiResponse);
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            throw new RuntimeException("OpenAI 응답에 'message'가 없습니다. 응답: " + apiResponse);
        }
        String content = (String) message.get("content");
        if (content == null || content.isBlank()) {
            throw new RuntimeException("OpenAI 응답에 'content'가 비어있습니다. 응답: " + apiResponse);
        }
        log.debug("추출된 content: {}", content);
        return content;
    }

    /**
     * 프롬프트 생성기
     */
    private static class PromptFactory {
        static String createDailyPlanPrompt(UUID scheduleId, LocalDate startDate, LocalDate endDate, LocalTime startTime, List<ItemWithLocationInfo> items) {
            String itemsJson;
            try {
                itemsJson = new ObjectMapper().writeValueAsString(items);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 직렬화 실패", e);
            }

            long travelDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            if (travelDays <= 0) travelDays = 1;

            int totalItemCount = items.size();
            long baseTotalCount = totalItemCount / travelDays;
            long remainderTotal = totalItemCount % travelDays;

            StringBuilder distributionInstruction = new StringBuilder();
            for (int i = 0; i < travelDays; i++) {
                long targetCount = baseTotalCount + (i < remainderTotal ? 1 : 0);
                distributionInstruction.append(String.format("* %d일차: 총 %d개%n", i + 1, targetCount));
            }

            return String.format("""
            너는 여행 일정 계획 전문가 AI다. 
            주어진 장소 목록을 **날짜별로 균등하게 배분**하고, 각 날짜의 `items` 배열 안에서는 **이동 경로가 자연스럽도록 순서**를 지정해야 한다.

            ### **규칙**
            1. 각 날짜의 `items` 개수는 [일자별 목표]와 반드시 일치해야 한다.
            2. 중복된 장소(`contentId`)는 절대 허용되지 않는다.
            3. category는 반드시 입력된 값 중 하나여야 한다. (ACCOMMODATION, RESTAURANT, TOURIST_SPOT, LEISURE, HEALING)
            4. 하루 일정의 순서는 다음 원칙을 따른다:
               - 첫 번째 장소는 그날의 출발지(전날 숙소 또는 첫날 시작점).
               - 마지막 장소는 숙소(`ACCOMMODATION`, 있으면).
               - 나머지는 지리적으로 가까운 순서로 배치한다.
            5. 하루 총 소요시간이 무리되지 않도록, 기본적으로 체류시간은 1~2시간이라고 가정한다.

            ### **[일자별 목표]**
            %s

            ### **출력 형식**
            ```json
            {
              "scheduleId": "%s",
              "dailyPlans": [
                {
                  "dayNumber": 1,
                  "items": [
                    {"contentId": "...", "title": "...", "latitude": ..., "longitude": ..., "category": "..."}
                  ]
                }
              ]
            }
            ```
3
            ### **입력 정보**
            * 여행 기간: %s ~ %s
            * 스케줄 ID: %s
            * 장소 목록:
            %s

            이제 규칙에 맞게 JSON을 생성하라.
            """,
                    distributionInstruction.toString(), scheduleId, startDate, endDate, scheduleId, itemsJson);
        }
    }
}
