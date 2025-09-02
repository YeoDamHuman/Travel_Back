package com.example.backend.schedule.service;

import com.example.backend.cart.entity.Cart;
import com.example.backend.common.auth.AuthUtil;
import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleCreateRequest;
import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleUpdateRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleDetailResponse;
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleInfo;
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleItemInfo;
import com.example.backend.schedule.filter.ScheduleFilter;
import com.example.backend.scheduleItem.service.ScheduleItemService;
import com.example.backend.group.entity.Group;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.entity.ScheduleType;
import com.example.backend.schedule.repository.ScheduleRepository;
import com.example.backend.scheduleItem.entity.ScheduleItem;
import com.example.backend.scheduleItem.repository.ScheduleItemRepository;
import com.example.backend.tour.webclient.TourApiClient;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.backend.common.auth.AuthUtil.getCurrentUserId;

/**
 * 스케줄 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 스케줄의 생성, 조회, 수정, 삭제 및 AI를 활용한 경로 최적화 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ScheduleItemRepository scheduleItemRepository;
    private final ScheduleFilter scheduleFilter;
    private final ScheduleItemService scheduleItemService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final TourApiClient tourApiClient;

    /**
     * 새로운 스케줄을 생성하고 스케줄 아이템들을 저장합니다.
     *
     * @param request 생성할 스케줄의 상세 정보가 담긴 {@link ScheduleCreateRequest} 객체.
     * @return 새로 생성된 스케줄의 ID.
     */
    @Transactional
    public UUID createSchedule(ScheduleCreateRequest request) {
        User user = AuthUtil.getCurrentUser(userRepository);
        Group group = scheduleFilter.validateScheduleRequest(request.getScheduleType(), request.getGroupId());
        Cart cart = scheduleFilter.validateCartExistence(request.getCartId());
        Schedule savedSchedule = scheduleRepository.save(ScheduleCreateRequest.toEntity(request, group, user, cart));
        List<ScheduleItem> scheduleItems = request.getScheduleItem().stream()
                .map(itemDto -> ScheduleItem.builder()
                        .contentId(itemDto.getContentId())
                        .cost(itemDto.getCost())
                        .scheduleId(savedSchedule)
                        .build())
                .collect(Collectors.toList());
        scheduleItemService.createItemList(scheduleItems);
        return savedSchedule.getScheduleId();
    }

    /**
     * 주어진 ID를 가진 스케줄을 찾습니다.
     *
     * @param scheduleId 찾을 스케줄의 ID.
     * @return 주어진 ID에 해당하는 {@link Schedule} 엔티티.
     * @throws IllegalArgumentException 스케줄을 찾을 수 없는 경우.
     */
    @Transactional
    public Schedule findScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케쥴을 찾을 수 없습니다."));
    }

    /**
     * 기존 스케줄의 정보를 업데이트합니다.
     *
     * @param request 업데이트할 스케줄의 ID와 새로운 정보가 담긴 {@link ScheduleUpdateRequest} 객체.
     * @return 업데이트된 스케줄의 ID.
     */
    @Transactional
    public UUID updateSchedule(ScheduleUpdateRequest request) {
        UUID currentUserId = getCurrentUserId();
        scheduleFilter.validateScheduleAccess(request.getScheduleId(), currentUserId);
        Schedule schedule = scheduleRepository.findById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 스케줄을 찾을 수 없습니다."));
        Group group = scheduleFilter.validateScheduleRequest(request.getScheduleType(), request.getGroupId());
        scheduleRepository.save(ScheduleUpdateRequest.toEntity(request, schedule, group));
        return schedule.getScheduleId();
    }

    /**
     * 주어진 ID를 가진 스케줄을 삭제합니다.
     *
     * @param scheduleId 삭제할 스케줄의 ID.
     */
    @Transactional
    public void deleteSchedule(UUID scheduleId) {
        UUID currentUserId = getCurrentUserId();
        scheduleFilter.validateScheduleAccess(scheduleId, currentUserId);
        scheduleRepository.deleteById(scheduleId);
    }

    /**
     * 현재 사용자의 스케줄 목록을 조회합니다.
     *
     * @param groupId 그룹 ID. null일 경우 개인 스케줄을 조회하고, 아닐 경우 해당 그룹의 스케줄을 조회합니다.
     * @return 조회된 스케줄 정보 목록.
     */
    @Transactional
    public List<scheduleInfo> getSchedules(UUID groupId) {
        UUID currentUserId = getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));
        List<Schedule> schedules;
        if (groupId != null) {
            Group groupEntity = scheduleFilter.validateGroupAccess(groupId, currentUserId);
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
                    return scheduleInfo.builder()
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
                            .scheduleStyle(schedule.getScheduleStyle())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 주어진 ID를 가진 스케줄의 상세 정보를 조회합니다.
     *
     * @param scheduleId 상세 정보를 조회할 스케줄의 ID.
     * @return 스케줄의 상세 정보와 포함된 스케줄 아이템 목록이 담긴 {@link scheduleDetailResponse} 객체.
     */
    @Transactional
    public scheduleDetailResponse getScheduleDetail(UUID scheduleId) {
        UUID currentUserId = getCurrentUserId();
        scheduleFilter.validateScheduleAccess(scheduleId, currentUserId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));

        List<ScheduleItem> scheduleItems = scheduleItemRepository.findAllByScheduleId_ScheduleId(scheduleId);

        List<String> contentIds = scheduleItems.stream()
                .map(ScheduleItem::getContentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> tourTitlesMap = tourApiClient.getTourTitlesMapByContentIds(contentIds);
        List<scheduleItemInfo> itemsDto = scheduleItems.stream()
                .map(item -> {
                    String title = tourTitlesMap.getOrDefault(item.getContentId(), "장소 이름 없음");
                    return scheduleItemInfo.builder()
                            .scheduleItemId(item.getScheduleItemId())
                            .contentId(item.getContentId())
                            .title(title)
                            .dayNumber(item.getDayNumber())
                            .startTime(item.getStartTime())
                            .endTime(item.getEndTime())
                            .memo(item.getMemo())
                            .cost(item.getCost())
                            .order(item.getOrder())
                            .build();
                })
                .collect(Collectors.toList());

        return scheduleDetailResponse.builder()
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

    /**
     * AI 서비스를 활용하여 스케줄의 경로를 최적화합니다.
     * <p>
     * AI로부터 최적화된 경로(JSON 형식)를 받아와 스케줄 아이템들을 업데이트하고 DB에 저장합니다.
     *
     * @param scheduleId 최적화할 스케줄의 ID.
     */
    @Transactional
    public void optimizeRoute(UUID scheduleId) {
        UUID currentUserId = getCurrentUserId();
        scheduleFilter.validateScheduleAccess(scheduleId, currentUserId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));

        List<ScheduleItem> items = scheduleItemRepository.findAllByScheduleId_ScheduleId(scheduleId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("해당 스케줄에 아이템이 없습니다.");
        }

        // 💡 1. contentId 목록 추출
        List<String> contentIds = items.stream()
                .map(ScheduleItem::getContentId)
                .distinct()
                .collect(Collectors.toList());

        // 💡 2. TourApiClient를 통해 위도/경도 정보 조회 (Map<String, Map<String, Double>> 형태)
        Map<String, Map<String, Double>> locationMap = tourApiClient.getTourLocationMapByContentIds(contentIds);

        // 💡 3. AiService에 전달할 데이터 목록 생성 (아이템 정보 + 위치 정보)
        List<AiService.ItemWithLocationInfo> itemsWithLocation = items.stream()
                .map(item -> {
                    Map<String, Double> loc = locationMap.getOrDefault(item.getContentId(), Collections.emptyMap());
                    double latitude = loc.getOrDefault("latitude", 0.0);
                    double longitude = loc.getOrDefault("longitude", 0.0);
                    return new AiService.ItemWithLocationInfo(item.getContentId(), latitude, longitude);
                })
                .collect(Collectors.toList());

        // 💡 4. 위도/경도 정보와 함께 AiService 호출
        String optimizedJson = aiService.getOptimizedRouteJson(schedule.getScheduleId(), schedule.getStartDate(), schedule.getEndDate(), itemsWithLocation)
                .block();

        // Mono의 flatMap 대신, block()으로 얻은 결과를 직접 처리합니다.
        try {
            Map<String, Object> responseMap = objectMapper.readValue(optimizedJson, new TypeReference<>() {});
            List<Map<String, Object>> optimizedItems = (List<Map<String, Object>>) responseMap.get("ScheduleItems");

            if (optimizedItems == null) {
                throw new RuntimeException("AI 응답에 'ScheduleItems' 필드가 없습니다.");
            }

            List<ScheduleItem> updatedItems = new ArrayList<>();

            for (Map<String, Object> itemData : optimizedItems) {
                String contentId = itemData.get("contentId").toString();
                int order = (int) itemData.get("order");
                int dayNumber = (int) itemData.get("dayNumber");
                String startTimeStr = (String) itemData.get("start_time");
                String endTimeStr = (String) itemData.get("end_time");

                items.stream()
                        .filter(item -> item.getContentId().equals(contentId))
                        .findFirst()
                        .ifPresent(originalItem -> {
                            ScheduleItem newItem = ScheduleItem.builder()
                                    .scheduleItemId(originalItem.getScheduleItemId())
                                    .contentId(originalItem.getContentId())
                                    .memo(originalItem.getMemo())
                                    .cost(originalItem.getCost())
                                    .scheduleId(originalItem.getScheduleId())
                                    .order(order)
                                    .dayNumber(dayNumber)
                                    .startTime(LocalTime.parse(startTimeStr))
                                    .endTime(LocalTime.parse(endTimeStr))
                                    .build();

                            updatedItems.add(newItem);
                        });
            }

            scheduleItemRepository.saveAll(updatedItems);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("AI 응답 JSON 파싱에 실패했습니다.", e);
        }
    }
}