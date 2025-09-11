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
     * AI를 이용해 최적화된 여행 경로를 두 단계에 걸쳐 생성합니다.
     * 1단계: 장소 목록을 날짜별로 그룹화하는 중간 계획을 생성합니다.
     * 2단계: 중간 계획을 바탕으로 일자별 동선을 최적화하고 최종 JSON을 생성합니다.
     */
    public Mono<String> getOptimizedRouteJson(UUID scheduleId, LocalDate startDate, LocalDate endDate, String startPlace, LocalTime startTime, List<ItemWithLocationInfo> itemsWithLocation) {
        log.info("🚀 AI 2-Step 경로 최적화 시작 - Schedule ID: {}", scheduleId);

        // 1단계: 날짜별 장소 그룹화 계획 요청
        log.info("▶️ [1/2] 중간 계획 생성 요청 시작");
        String firstPrompt = PromptFactory.createFirstStepPrompt(scheduleId, startDate, endDate, startTime, itemsWithLocation);
        return callOpenAiApi(firstPrompt)
                .flatMap(intermediatePlanJson -> {
                    log.info("✅ [1/2] 중간 계획 수신 성공");
                    log.debug("📄 중간 계획 JSON: {}", intermediatePlanJson);

                    // 2단계: 경로 최적화 및 최종 JSON 포맷팅 요청
                    log.info("▶️ [2/2] 최종 경로 최적화 및 포맷팅 요청 시작");
                    String secondPrompt = PromptFactory.createSecondStepPrompt(scheduleId, startPlace, intermediatePlanJson);
                    return callOpenAiApi(secondPrompt);
                })
                .doOnSuccess(finalJson -> log.info("✅ [2/2] 최종 경로 최적화 JSON 수신 성공! - Schedule ID: {}", scheduleId))
                .onErrorMap(throwable -> {
                    log.error("❌ OpenAI API 호출 중 심각한 오류 발생 - Schedule ID: {}", scheduleId, throwable);
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

    /**
     * API 응답에서 실제 content(JSON 문자열)를 추출합니다.
     */
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
         * 1단계: 장소들을 규칙에 따라 날짜별로 배정하고 중간 계획 JSON을 생성하는 프롬프트
         */
        static String createFirstStepPrompt(UUID scheduleId, LocalDate startDate, LocalDate endDate, LocalTime startTime, List<ItemWithLocationInfo> items) {
            String itemsJson;
            try {
                // ObjectMapper는 외부에서 주입받을 수 없으므로 여기서 직접 생성합니다.
                itemsJson = new ObjectMapper().writeValueAsString(items);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 직렬화에 실패했습니다.", e);
            }

            return String.format("""
                너는 여행 일정 계획 전문가 AI다. 너의 첫 번째 임무는 주어진 장소 목록을 핵심 규칙에 따라 각 여행일에 논리적으로 배정하고, 중간 계획을 지정된 JSON 형식으로 반환하는 것이다.

                ### **[1] 최종 목표**
                모든 규칙을 준수하여, 각 장소를 방문할 날짜(`dayNumber`)만 결정하여 그룹화한다. (방문 순서 `order`는 다음 단계에서 결정하므로 아직 신경쓰지 않는다.)

                ### **[2] 핵심 작업 규칙**
                [A] 숙소 배정 규칙 (최우선 순위!):
                * `category`가 `ACCOMMODATION`인 장소를 식별한다.
                * 1일차: 첫 번째 숙소를 1일차 그룹에 포함시킨다. 이 숙소는 그날의 마지막 장소가 될 것이다.
                * 중간일 (2일차 ~ 마지막 전날): 이전 날의 숙소를 해당일 그룹의 첫 장소로, 다음 순서의 숙소를 해당일 그룹의 마지막 장소로 포함시킨다.
                * 마지막 날: 이전 날의 숙소를 마지막 날 그룹의 첫 장소로 포함시킨다.

                [B] 카테고리별 일정 계획 규칙:
                * 식당(`RESTAURANT`): 각 날짜별로 점심, 저녁에 방문하도록 하루 2개씩 배정하는 것을 기본으로 한다.
                * 기타 장소(`TOURIST_SPOT`, `LEISURE`, `HEALING`): 숙소와 식당 배정 후, 남은 장소들을 전체 여행 기간에 걸쳐 균등하게 분배한다.

                ### **[3] 중간 출력 데이터 형식 (매우 중요!)**
                너의 응답은 반드시 아래 JSON 구조를 따라야 한다. 다른 설명 없이, 순수한 JSON 객체 하나만 출력해야 한다. `items` 배열에는 해당일에 방문할 장소의 원본 정보를 그대로 넣는다.
                ```json
                {
                  "scheduleId": "입력받은 스케줄 ID",
                  "dailyPlans": [
                    {
                      "dayNumber": 1,
                      "items": [
                        {"contentId": "...", "title": "...", "latitude": ..., "longitude": ..., "category": "..."},
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

                이제 위의 규칙에 따라 중간 계획 JSON을 생성해줘.
                """,
                    startDate, endDate, scheduleId, itemsJson);
        }

        /**
         * 2단계: 1단계에서 생성된 중간 계획을 받아, 날짜별로 경로를 최적화하고 최종 JSON을 생성하는 프롬프트
         */
        static String createSecondStepPrompt(UUID scheduleId, String startPlace, String intermediatePlanJson) {
            return String.format("""
                너는 세계 최고의 여행 경로 최적화 전문가 AI다. 너의 임무는 주어진 날짜별 장소 그룹 목록을 바탕으로, 각 날짜 내에서 이동 거리가 가장 짧아지는 최적의 방문 순서(`order`)를 결정하고, 반드시 지정된 최종 JSON 형식으로 결과를 반환하는 것이다.

                ### **[1] 최종 목표**
                각 장소의 방문일(`dayNumber`)과 최적화된 방문 순서(`order`)를 결정하여 최종 결과물을 완성한다.

                ### **[2] 핵심 작업 규칙**
                * 각 `dailyPlans` 배열에 포함된 장소 목록을 대상으로 작업한다.
                * **1일차:** 입력받은 `최초 출발 장소`에서 시작하여 그날의 모든 장소를 가장 효율적으로 방문하는 순서를 결정한다.
                * **2일차 이후:** 전날 마지막 장소(주로 숙소)에서 시작하여 그날의 모든 장소를 가장 효율적으로 방문하는 순서를 결정한다.
                * `category`가 `ACCOMMODATION`인 장소는 해당일의 시작 또는 마지막 방문지여야 한다는 점을 반드시 고려해야 한다.
                * 위도(`latitude`)와 경도(`longitude`) 정보를 활용하여 지리적으로 가장 가까운 순서대로 `order`를 부여한다.

                ### **[3] 최종 출력 데이터 형식 (매우 중요!)**
                너의 최종 응답은 반드시 아래 JSON 구조를 따라야 한다. 코드 블록 마커나 다른 설명 없이, 순수한 JSON 객체 하나만 출력해야 한다.
                ```json
                {
                  "scheduleId": "입력받은 스케줄 ID",
                  "scheduleItems": [
                    {
                      "order": 1,
                      "contentId": "장소의 contentId",
                      "dayNumber": 1
                    }
                  ]
                }
                ```

                ### **[4] 입력 정보 (중간 계획)**
                * 스케줄 ID: %s
                * 1일차 최초 출발 장소: %s
                * 날짜별 장소 그룹 목록 (중간 계획):
                %s

                이제, 이 중간 계획을 바탕으로 경로를 최적화하고, 최종 JSON을 출력해줘.
                """,
                    scheduleId, startPlace, intermediatePlanJson);
        }
    }
}