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
     * AI를 이용해 주어진 장소 목록을 날짜별로 그룹화하는 '중간 계획 JSON'을 생성합니다.
     * 이 서비스의 결과물은 Kakao Map API 등을 활용하는 'RouteOptimizerService'로 전달되어
     * 최종 동선 최적화에 사용됩니다.
     *
     * @return 날짜별로 장소가 배정된 중간 계획 JSON을 담은 Mono<String>
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
     * OpenAI API를 호출하고 응답의 'content'를 추출하는 공통 메소드
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
                .doOnNext(response -> {
                    try {
                        log.debug("⬅️ OpenAI API 원본 응답: {}", objectMapper.writeValueAsString(response));
                    } catch (JsonProcessingException e) {
                        log.warn("API 응답 JSON 변환 실패 (로깅 목적)", e);
                    }
                })
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
     * AI 요청 프롬프트를 생성하는 역할을 담당하는 정적 중첩 클래스
     */
    private static class PromptFactory {

        /**
         * 장소들을 규칙에 따라 날짜별로 배정하고 중간 계획 JSON을 생성하는 프롬프트
         */
        static String createDailyPlanPrompt(UUID scheduleId, LocalDate startDate, LocalDate endDate, LocalTime startTime, List<ItemWithLocationInfo> items) {
            String itemsJson;
            try {
                itemsJson = new ObjectMapper().writeValueAsString(items);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 직렬화에 실패했습니다.", e);
            }

            return String.format("""
                너는 여행 일정 계획 전문가 AI다. 너의 임무는 주어진 장소 목록을 핵심 규칙에 따라 각 여행일에 논리적으로 배정하고, 지정된 JSON 형식으로 반환하는 것이다.

                ### **[1] 최종 목표**
                모든 규칙을 준수하여, 각 장소를 방문할 날짜(`dayNumber`)만 결정하여 그룹화한다. (방문 순서는 이 단계에서 결정하지 않는다.)

                ### **[2] 핵심 작업 규칙**
                [A] 숙소 배정 규칙 (최우선 순위!):
                * `category`가 `ACCOMMODATION`인 장소를 식별한다.
                * 1일차: 첫 번째 숙소를 1일차 그룹에 포함시킨다. 이 숙소는 그날의 마지막 장소가 될 것이다.
                * 중간일 (2일차 ~ 마지막 전날): 이전 날의 숙소를 해당일 그룹의 첫 장소로, 다음 순서의 숙소를 해당일 그룹의 마지막 장소로 포함시킨다.
                * 마지막 날: 이전 날의 숙소를 마지막 날 그룹의 첫 장소로 포함시킨다.

                [B] 카테고리별 일정 계획 규칙:
                * 식당(`RESTAURANT`): 각 날짜별로 점심, 저녁에 방문하도록 하루 2개씩 배정하는 것을 기본으로 한다.
                * 기타 장소(`TOURIST_SPOT`, `LEISURE`, `HEALING`): 숙소와 식당 배정 후, 남은 장소들을 전체 여행 기간에 걸쳐 균등하게 분배한다.

                ### **[3] 출력 데이터 형식 (매우 중요!)**
                너의 응답은 반드시 아래 JSON 구조를 따라야 한다. 다른 설명 없이, 순수한 JSON 객체 하나만 출력해야 한다.
                ```json
                {
                  "scheduleId": "입력받은 스케줄 ID",
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

                ### **[4] 입력 정보**
                * 여행 기간: %s 부터 %s 까지
                * 스케줄 ID: %s
                * 스케줄 아이템 목록:
                %s

                이제 위의 규칙에 따라 일자별 계획 JSON을 생성해줘.
                """,
                    startDate, endDate, scheduleId, itemsJson);
        }
    }
}