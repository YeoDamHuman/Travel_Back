package com.example.backend.schedule.service;

import com.example.backend.scheduleItem.entity.ScheduleItem;
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

    public Mono<String> getOptimizedRouteJson(UUID scheduleId, LocalDate startDate, LocalDate endDate, List<ScheduleItem> items) {
        String prompt = createOptimizationPrompt(scheduleId, startDate, endDate, items);
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return webClient.post()
                .uri("/v1/chat/completions")
                .headers(h -> h.addAll(headers))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractContentFromApiResponse)
                .onErrorMap(throwable -> new RuntimeException("OpenAI API 호출 실패: " + throwable.getMessage(), throwable));
    }

    private String createOptimizationPrompt(UUID scheduleId, LocalDate startDate, LocalDate endDate, List<ScheduleItem> items) {
        List<ScheduleItemInfo> itemInfos = items.stream()
                .map(item -> new ScheduleItemInfo(item.getContentId()))
                .collect(Collectors.toList());

        String itemsJson;
        try {
            itemsJson = objectMapper.writeValueAsString(itemInfos);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화에 실패했습니다.", e);
        }

        return String.format("""
            너는 여행 경로 최적화 전문가야.
            아래 정보를 참고해서 일정 아이템들의 효율적인 '순서(order)'를 정렬하고, 각 아이템이 어느 '몇일차(dayNumber)'에 속하는지 할당하고, 적절한 'start_time'과 'end_time'도 할당해줘.
            시간은 24시간 형식(HH:mm)으로 반환해줘.
            추가적으로 각각에 dayNumber에 같은 갯수의 아이템을 배분해줘.(ex 1일차 3개 2일차 3개)
            그리고 경로 최적화는 최대한 가까운 거리의 아이템끼리 묶어줘.
            여행 기간은 %s 부터 %s 까지야.
            
            📌 스케줄 ID: %s
            📌 현재 스케줄 아이템 리스트: 
            %s
            
            아래 JSON 구조를 절대 변경하지 말고, 다른 필드는 절대 넣지 말고,
            정확하게 아래의 JSON 형식으로만 출력해줘:
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
            
            ❗️ 다른 설명이나 말은 절대 추가하지 말고, 오직 JSON 객체만 출력해.
            """,
                startDate,
                endDate,
                scheduleId,
                itemsJson,
                scheduleId
        );
    }

    private String extractContentFromApiResponse(Map<String, Object> apiResponse) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) apiResponse.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("OpenAI 응답에 'choices'가 없습니다.");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) {
            throw new RuntimeException("OpenAI 응답에 'message'가 없습니다.");
        }
        String content = (String) message.get("content");
        if (content == null || content.isBlank()) {
            throw new RuntimeException("OpenAI 응답에 'content'가 비어있습니다.");
        }
        return content;
    }

    private record ScheduleItemInfo(UUID contentId) {}
}