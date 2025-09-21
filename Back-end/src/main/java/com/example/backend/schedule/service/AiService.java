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

            long travelDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
            if (travelDays <= 0) {
                travelDays = 1;
            }

            int totalItemCount = items.size();
            long baseTotalCount = totalItemCount / travelDays;
            long remainderTotal = totalItemCount % travelDays;
            long[] totalItemsPerDay = new long[(int) travelDays];
            for (int i = 0; i < travelDays; i++) {
                totalItemsPerDay[i] = baseTotalCount + (i < remainderTotal ? 1 : 0);
            }

            long accommodationsCount = items.stream().filter(item -> "ACCOMMODATION".equals(item.category())).count();
            long restaurantsCount = items.stream().filter(item -> "RESTAURANT".equals(item.category())).count();
            long avgRestaurantsPerDay = (restaurantsCount > 0 && travelDays > 0) ? (long) Math.ceil((double) restaurantsCount / travelDays) : 2;

            StringBuilder distributionInstruction = new StringBuilder();
            for (int i = 0; i < travelDays; i++) {
                long fixedItemsThisDay = 0;
                if (travelDays == 1) {
                    fixedItemsThisDay += Math.min(1, accommodationsCount);
                } else {
                    if (i == 0 || i == travelDays - 1) {
                        fixedItemsThisDay += (accommodationsCount > 0) ? 1 : 0;
                    } else {
                        fixedItemsThisDay += (accommodationsCount > 1) ? 2 : 0;
                    }
                }
                fixedItemsThisDay += avgRestaurantsPerDay;

                long otherItemsToAdd = totalItemsPerDay[i] - fixedItemsThisDay;
                otherItemsToAdd = Math.max(0, otherItemsToAdd);

                distributionInstruction.append(String.format("* %d일차: '기타 장소' %d개 추가 (최종 목표: 총 %d개)%n", i + 1, otherItemsToAdd, totalItemsPerDay[i]));
            }

            return String.format("""
                너는 여행 일정 계획 전문가 AI다. 너의 임무는 주어진 장소 목록을 핵심 규칙에 따라 각 여행일에 논리적으로 배정하여, **일자별 총 장소 개수 목표**를 정확히 맞추는 것이다.

                ### **[1] 핵심 목표**
                각 날짜의 `items` 배열에 포함될 장소의 **총개수**가 아래 **[일자별 최종 목표]** 표에 명시된 '최종 목표' 숫자와 정확히 일치하도록 만들어야 한다.

                ### **[2] 작업 규칙**
                장소들은 숙소, 식당, 기타 장소 세 종류로 나뉜다.
                1.  **숙소(`ACCOMMODATION`)와 식당(`RESTAURANT`)을 규칙에 따라 먼저 마음속으로 배정한다.**
                    * 숙소 규칙: 1일차와 마지막 날은 1개, 중간 날은 2개(전날 숙소, 당일 숙소)가 기본이다.
                    * 식당 규칙: 하루 2개 배정을 목표로 한다.
                2.  **그 다음, 아래 [일자별 최종 목표] 표를 확인한다.**
                3.  표에 적힌 **'기타 장소 추가 개수'만큼** `TOURIST_SPOT`, `LEISURE`, `HEALING` 카테고리에서 장소를 골라 추가한다.
                4.  이렇게 조합하여 최종적으로 그날의 **'최종 목표' 총개수를 정확히 맞춘다.** 지리적 근접성을 고려하여 장소를 선택하면 가장 좋다.

                ### **[일자별 최종 목표]**
                **아래 지시에 따라 '기타 장소'를 추가하여, 일자별 '최종 목표' 총개수를 반드시 맞춰야 한다.**
                %s

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
                    distributionInstruction.toString(),
                    startDate, endDate, scheduleId, itemsJson);
        }
    }
}