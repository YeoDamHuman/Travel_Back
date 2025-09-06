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

    public Mono<String> getOptimizedRouteJson(UUID scheduleId, LocalDate startDate, LocalDate endDate, String startPlace, LocalTime startTime, List<ItemWithLocationInfo> itemsWithLocation) {
        log.info("🚀 AI 경로 최적화 시작 - Schedule ID: {}", scheduleId);

        String prompt = createOptimizationPrompt(scheduleId, startDate, endDate, startPlace, startTime, itemsWithLocation);
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

    private String createOptimizationPrompt(UUID scheduleId, LocalDate startDate, LocalDate endDate, String startPlace, LocalTime startTime, List<ItemWithLocationInfo> items) {
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
            너는 세계 최고의 여행 일정 최적화 전문가 AI다. 너의 임무는 주어진 여행 정보와 장소 목록을 바탕으로, 가장 효율적이고 논리적인 여행 계획을 세운 뒤, 반드시 지정된 JSON 형식으로만 결과를 반환하는 것이다. 다른 부가적인 설명은 절대 포함해서는 안 된다.

            ---
            ### **[1] 최종 목표**
            모든 제약 조건을 준수하여, 여행 기간 내 각 장소의 방문일(`dayNumber`)과 방문 순서(`order`)를 결정한다.

            ---
            ### **[2] 입력 데이터 형식**
            너는 아래와 같은 형식의 데이터를 입력받게 될 것이다.
            * `scheduleId`: 여행 일정의 고유 ID (문자열)
            * `dateRange`: 여행 기간 (예: "2025-07-01 to 2025-07-10")
            * `startPlace`: 첫째 날 여행 시작 장소 (예: "서울역")
            * `startTime`: 첫째 날 여행 시작 시간 (예: "09:00")
            * `items`: 방문할 장소 목록 (JSON 배열). 각 장소는 `contentId`, `title`, `latitude`, `longitude`, `category` 정보를 포함한다.

            ---
            ### **[3] 출력 데이터 형식 (매우 중요!)**
            너의 최종 응답은 반드시 아래 JSON 구조를 따라야 한다. 코드 블록 마커나 다른 설명 없이, 순수한 JSON 객체 하나만 출력해야 한다.
            ```json
            {
              "scheduleId": "입력받은 스케줄 ID",
              "ScheduleItems": [
                {
                  "order": 1,
                  "contentId": "장소의 contentId",
                  "dayNumber": 1
                }
              ]
            }
            ```

            ---
            ### **[4] 핵심 작업 규칙**

            **[A] 시간 제약 조건 (내부 계산용)**
            * 하루 활동 시간은 **09:00부터 22:00까지**로 가정한다.
            * 모든 장소(숙소 제외)는 평균 **2시간** 머무는 것으로 계산한다.
            * 장소 간 이동 시간은 평균 **30분**으로 계산한다.
            * 이 시간 정보는 최적의 일정을 짜기 위한 너의 내부 계산에만 사용하고, 최종 JSON 출력에는 포함하지 않는다.

            **[B] 숙소 배정 규칙 (최우선 순위!)**
            * `items` 목록에서 `category`가 `ACCOMMODATION`인 장소들을 먼저 식별한다.
            * **1일차:** `items`에 포함된 **첫 번째 숙소**를 1일차 일정의 **마지막(`order`가 가장 큼) 장소**로 배정한다.
            * **중간일 (2일차 ~ 마지막 전날):**
                1. **이전 날의 숙소**를 해당일의 **첫 번째(`order`: 1) 장소**로 배정한다.
                2. `items`에 포함된 **다음 순서의 숙소**를 해당일의 **마지막 장소**로 배정한다.
            * **마지막 날:** **이전 날의 숙소**를 마지막 날의 **첫 번째(`order`: 1) 장소**로 배정한다. 그 이후로는 숙소를 배정하지 않는다.

            **[C] 카테고리별 일정 계획 규칙**
            * **숙소(`ACCOMMODATION`)**: 규칙 [B]에 따라 가장 먼저 배정한다.
            * **식당(`RESTAURANT`)**: 각 날짜별로 **점심(12:00~14:00), 저녁(18:00~20:00) 시간대**에 방문하도록 일정을 구성한다. 하루에 2개의 식당을 배정하는 것을 기본으로 한다.
            * **기타 장소(`TOURIST_SPOT`, `LEISURE`, `HEALING`):** 숙소와 식당이 배정된 사이의 빈 시간대에 채워 넣는다.

            **[D] 균등 분배 및 경로 최적화 규칙**
            1. 규칙 [B]와 [C]에 따라 숙소와 식당을 각 날짜에 먼저 배정한다.
            2. 남아있는 **기타 장소**들을 **(총 장소 개수 / 총 여행일수)** 계산에 따라 각 날짜에 균등하게 분배한다.
            3. 각 날짜별로 배정된 모든 장소들에 대해, `startPlace`와 위도/경도를 기반으로 **전체 이동 거리가 가장 짧아지는 방문 순서(`order`)**를 결정한다.

            ---
            ### **[5] 사고 과정 (반드시 따를 것)**
            아래의 단계를 순서대로 반드시 따라서 최종 JSON을 생성해야 한다.
            1.  **입력 분석**: `scheduleId`, 여행 기간, 출발 정보, 장소 목록(`items`)을 정확히 파악하고 전체 여행 일수를 계산한다.
            2.  **장소 분류**: `items` 목록을 `ACCOMMODATION`, `RESTAURANT`, `OTHERS` 세 그룹으로 분류한다.
            3.  **숙소 배정**: 규칙 [B]에 따라, 각 날짜별 일정 목록에 숙소를 먼저 배치한다.
            4.  **식당 배정**: 규칙 [C]에 따라, 남은 식당들을 각 날짜의 점심/저녁 시간 슬롯에 맞게 배정한다.
            5.  **나머지 장소 배정**: `OTHERS` 그룹의 장소들을 규칙 [D]에 따라 각 날짜에 균등하게 분배한다.
            6.  **일자별 경로 최적화**: 각 날짜별로 완성된 장소 목록을 대상으로, 출발지부터 시작하여 전체 이동 거리가 최소화되도록 방문 순서(`order`)를 최종 결정한다.
            7.  **최종 JSON 생성**: 위 6단계까지의 결과를 바탕으로, 규칙 [3]에서 정의한 JSON 출력 형식에 맞춰 최종 결과물을 생성한다.

            ---
            ### **[6] 입력 정보**
            * 여행 기간: %s 부터 %s 까지
            * 최초 출발 장소: %s
            * 최초 출발 시간: %s
            * 스케줄 ID: %s
            * 스케줄 아이템 목록:
            %s

            이제, 위의 모든 규칙과 사고 과정을 거쳐 최종 JSON을 출력해줘.
            """,
                startDate,
                endDate,
                startPlace,
                startTime,
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