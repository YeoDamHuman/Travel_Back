package com.example.backend.schedule.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.backend.group.entity.Group;
import com.example.backend.group.repository.GroupRepository;
import com.example.backend.schedule.dto.request.ScheduleRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.entity.ScheduleType;
import com.example.backend.schedule.repository.ScheduleRepository;
import com.example.backend.scheduleItem.entity.ScheduleItem;
import com.example.backend.scheduleItem.repository.ScheduleItemRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final ScheduleItemRepository scheduleItemRepository;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return UUID.fromString(authentication.getName());
    }

    @Transactional
    public UUID createSchedule(ScheduleRequest.scheduleCreateRequest request) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        Group groupEntity = null;
        if (request.getScheduleType() == ScheduleType.GROUP) {
            if (request.getGroupId() == null) {
                throw new IllegalArgumentException("그룹 스케줄 생성 시 groupId는 필수입니다.");
            }
            groupEntity = groupRepository.findByIdWithUsers(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

            boolean isMember = groupEntity.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("해당 그룹의 멤버가 아닙니다. 그룹 스케줄을 생성할 수 없습니다.");
            }
        } else if (request.getScheduleType() == ScheduleType.PERSONAL) {
            if (request.getGroupId() != null) {
                throw new IllegalArgumentException("개인 스케줄 생성 시 groupId는 null이어야 합니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 스케줄 타입입니다.");
        }

        Schedule schedule = Schedule.builder()
                .scheduleName(request.getScheduleName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .budget(request.getBudget())
                .groupId(groupEntity)
                .userId(currentUser)
                .scheduleType(request.getScheduleType())
                .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);
        return savedSchedule.getScheduleId();
    }

    @Transactional
    public UUID updateSchedule(ScheduleRequest.scheduleUpdateRequest request) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 스케줄을 찾을 수 없습니다."));

        if (schedule.getScheduleType() == ScheduleType.PERSONAL) {
            if (!schedule.getUserId().getUserId().equals(currentUserId)) {
                throw new IllegalStateException("개인 스케줄에 대한 수정 권한이 없습니다.");
            }
        } else if (schedule.getScheduleType() == ScheduleType.GROUP) {
            Group groupEntity = groupRepository.findByIdWithUsers(schedule.getGroupId().getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 스케줄의 그룹을 찾을 수 없습니다."));

            boolean isMember = groupEntity.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("그룹 멤버가 아니어서 스케줄을 수정할 수 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 스케줄 타입입니다.");
        }

        Group groupEntity = null;
        if (request.getScheduleType() == ScheduleType.GROUP) {
            if (request.getGroupId() == null) {
                throw new IllegalArgumentException("그룹 스케줄 수정 시 groupId는 필수입니다.");
            }
            groupEntity = groupRepository.findByIdWithUsers(request.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

            boolean isMember = groupEntity.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("해당 그룹의 멤버가 아닙니다.");
            }
        } else if (request.getScheduleType() == ScheduleType.PERSONAL) {
            if (request.getGroupId() != null) {
                throw new IllegalArgumentException("개인 스케줄 수정 시 groupId는 null이어야 합니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 스케줄 타입입니다.");
        }

        schedule.updateSchedule(
                request.getScheduleName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getBudget(),
                groupEntity,
                request.getScheduleType()
        );

        scheduleRepository.save(schedule);
        return schedule.getScheduleId();
    }

    @Transactional
    public void deleteSchedule(UUID scheduleId) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("삭제하려는 스케줄을 찾을 수 없습니다."));

        if (schedule.getScheduleType() == ScheduleType.PERSONAL) {
            if (!schedule.getUserId().getUserId().equals(currentUserId)) {
                throw new IllegalStateException("개인 스케줄에 대한 삭제 권한이 없습니다.");
            }
        } else if (schedule.getScheduleType() == ScheduleType.GROUP) {
            Group group = groupRepository.findByIdWithUsers(schedule.getGroupId().getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 스케줄의 그룹을 찾을 수 없습니다."));

            boolean isMember = group.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("그룹 멤버가 아니어서 스케줄을 삭제할 수 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 스케줄 타입입니다.");
        }

        scheduleRepository.delete(schedule);
    }

    @Transactional
    public List<ScheduleResponse.scheduleInfo> getSchedules(UUID groupId) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        List<Schedule> schedules;

        if (groupId != null) {
            Group groupEntity = groupRepository.findByIdWithUsers(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

            boolean isMember = groupEntity.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("해당 그룹의 멤버가 아닙니다. 그룹 스케줄을 조회할 수 없습니다.");
            }
            schedules = scheduleRepository.findAllByGroupId(groupEntity);
            schedules = schedules.stream()
                    .filter(s -> s.getScheduleType() == ScheduleType.GROUP)
                    .collect(Collectors.toList());
        } else {
            schedules = scheduleRepository.findAllByUserIdAndScheduleType(currentUser, ScheduleType.PERSONAL);
        }

        return schedules.stream()
                .map(schedule -> {
                    UUID responseGroupId = (schedule.getScheduleType() == ScheduleType.GROUP && schedule.getGroupId() != null)
                            ? schedule.getGroupId().getGroupId() : null;
                    String responseGroupName = (schedule.getScheduleType() == ScheduleType.GROUP && schedule.getGroupId() != null)
                            ? schedule.getGroupId().getGroupName() : null;

                    return ScheduleResponse.scheduleInfo.builder()
                            .scheduleId(schedule.getScheduleId())
                            .scheduleName(schedule.getScheduleName())
                            .startDate(schedule.getStartDate())
                            .endDate(schedule.getEndDate())
                            .createdAt(schedule.getCreatedAt())
                            .updatedAt(schedule.getUpdatedAt())
                            .budget(schedule.getBudget())
                            .groupId(responseGroupId)
                            .groupName(responseGroupName)
                            .userId(schedule.getUserId().getUserId())
                            .scheduleType(schedule.getScheduleType().name())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ScheduleResponse.scheduleDetailResponse getScheduleDetail(UUID scheduleId) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));

        if (schedule.getScheduleType() == ScheduleType.PERSONAL) {
            if (!schedule.getUserId().getUserId().equals(currentUserId)) {
                throw new IllegalStateException("개인 스케줄에 접근할 수 없습니다.");
            }
        } else if (schedule.getScheduleType() == ScheduleType.GROUP) {
            Group group = groupRepository.findByIdWithUsers(schedule.getGroupId().getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("해당 스케줄의 그룹을 찾을 수 없습니다."));
            boolean isMember = group.getUsers().stream()
                    .anyMatch(user -> user.getUserId().equals(currentUserId));
            if (!isMember) {
                throw new IllegalStateException("해당 그룹의 멤버가 아닙니다. 스케줄을 조회할 수 없습니다.");
            }
        } else {
            throw new IllegalArgumentException("알 수 없는 스케쥴 타입입니다.");
        }

        List<ScheduleItem> scheduleItems = scheduleItemRepository.findAllByScheduleId(schedule);

        List<ScheduleResponse.scheduleItemInfo> itemsDto = scheduleItems.stream()
                .map(item -> ScheduleResponse.scheduleItemInfo.builder()
                        .scheduleItemId(item.getScheduleItemId())
                        .placeId(item.getPlaceId())
                        .dayNumber(item.getDayNumber())
                        .startTime(item.getStartTime())
                        .endTime(item.getEndTime())
                        .memo(item.getMemo())
                        .cost(item.getCost())
                        .build())
                .collect(Collectors.toList());

        return ScheduleResponse.scheduleDetailResponse.builder()
                .scheduleId(schedule.getScheduleId())
                .scheduleName(schedule.getScheduleName())
                .startDate(schedule.getStartDate())
                .endDate(schedule.getEndDate())
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .budget(schedule.getBudget())
                .scheduleItems(itemsDto)
                .build();
    }

    public Mono<ScheduleResponse.OptimizeRouteResponse> optimizeRoute(UUID scheduleId, ScheduleRequest.OptimizeRouteRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케쥴을 찾을 수 없습니다."));
        List<ScheduleItem> scheduleItems = scheduleItemRepository.findAllByScheduleId(schedule);

        if (scheduleItems.isEmpty()) {
            throw new IllegalArgumentException("해당 스케줄에 아이템이 없습니다.");
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        List<ScheduleResponse.scheduleItemInfo> scheduleItemsDto = scheduleItems.stream()
                .map(item -> ScheduleResponse.scheduleItemInfo.builder()
                        .scheduleItemId(item.getScheduleItemId())
                        .placeId(item.getPlaceId())
                        .dayNumber(item.getDayNumber())
                        .startTime(item.getStartTime())
                        .endTime(item.getEndTime())
                        .memo(item.getMemo())
                        .cost(item.getCost())
                        .build())
                .collect(Collectors.toList());

        String preferencesJson;
        String itemsJson;

        try {
            preferencesJson = mapper.writeValueAsString(request.getPreferences());
            itemsJson = mapper.writeValueAsString(scheduleItemsDto);
        } catch (Exception e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }

        String prompt = String.format("""
너는 여행 경로 최적화 전문가야.
아래 정보를 참고해서 일정 아이템들을 효율적으로 순서를 정렬하고 최적 경로를 추천해.
각 아이템이 어느 '몇일차(dayNumber)'에 속하는지도 출력해줘.

📌 스케줄 ID: %s
📌 최적화 타입: %s
📌 선호사항: %s
📌 현재 스케줄 아이템 리스트:
%s

아래 JSON 구조를 절대 변경하지 말고, 다른 필드는 절대 넣지 말고,
정확하게 아래의 JSON 형식으로만 출력해줘:
{
  "scheduleId": "스케줄 ID",
  "optimizeRoute": [
    {
      "order": 1,
      "location": "장소 UUID",
      "estimatedTimeMinutes": 30,
      "distanceKm": 12.5,
      "dayNumber": 1
    }
  ]
}

❗️ 절대 설명 문장 쓰지 말고 JSON만 출력해.
""", scheduleId, request.getOptimizationType(), preferencesJson, itemsJson);

        Map<String, Object> body = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        return WebClient.builder()
                .baseUrl(openAiApiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openAiApiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build()
                .post()
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        if (message != null) {
                            String content = (String) message.get("content");
                            System.out.println("GPT 응답: " + content);

                            if (!content.trim().startsWith("{")) {
                                throw new RuntimeException("GPT 응답이 JSON 형식이 아님: " + content);
                            }

                            try {
                                ScheduleResponse.OptimizeRouteResponse result = mapper.readValue(content, ScheduleResponse.OptimizeRouteResponse.class);

                                List<ScheduleItem> scheduleItemsFull = scheduleItemRepository.findAllByScheduleId(schedule);

                                List<ScheduleResponse.RouteStep> enhancedRoute = new ArrayList<>();
                                for (ScheduleResponse.RouteStep step : result.getOptimizeRoute()) {
                                    Optional<ScheduleItem> matchingItem = scheduleItemsFull.stream()
                                            .filter(item -> item.getPlaceId().toString().equals(step.getLocation()))
                                            .findFirst();

                                    Integer dayNumber = matchingItem.map(ScheduleItem::getDayNumber).orElse(null);

                                    ScheduleResponse.RouteStep stepWithDay = ScheduleResponse.RouteStep.builder()
                                            .order(step.getOrder())
                                            .location(step.getLocation())
                                            .estimatedTimeMinutes(step.getEstimatedTimeMinutes())
                                            .distanceKm(step.getDistanceKm())
                                            .dayNumber(dayNumber)
                                            .build();

                                    enhancedRoute.add(stepWithDay);
                                }

                                return ScheduleResponse.OptimizeRouteResponse.builder()
                                        .scheduleId(result.getScheduleId())
                                        .optimizeRoute(enhancedRoute)
                                        .build();

                            } catch (Exception e) {
                                throw new RuntimeException("GPT 응답 JSON 파싱 실패: " + content, e);
                            }
                        }
                    }
                    throw new RuntimeException("OpenAI 응답에서 결과를 찾을 수 없습니다.");
                });
    }
}
