package com.example.backend.schedule.service;

import com.example.backend.cart.entity.Cart;
import com.example.backend.common.auth.AuthUtil;
import com.example.backend.map.dto.request.MapRequest;
import com.example.backend.map.dto.response.MapResponse;
import com.example.backend.map.service.MapService;
import com.example.backend.region.service.RegionService;
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
import com.example.backend.tour.entity.TourCategory;
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

import java.time.temporal.ChronoUnit;
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
    private final RegionService regionService;
    private final MapService mapService;
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
                            .isBoarded(schedule.isBoarded())
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
    @Transactional(readOnly = true)
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
        Map<String, Map<String, String>> tourExtraInfoMap = tourApiClient.getTourExtraInfoMapByContentIds(contentIds);

        List<RegionService.CodePair> codePairsToSearch = tourExtraInfoMap.values().stream()
                .map(info -> new RegionService.CodePair(
                        info.get("lDongRegnCd"),
                        info.get("lDongSignguCd")
                ))
                .filter(pair -> pair.lDongRegnCd() != null && !pair.lDongRegnCd().isEmpty())
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> regionNameMap = regionService.getRegionNamesByCodePairs(codePairsToSearch);

        List<scheduleItemInfo> itemsDto = scheduleItems.stream()
                .map(item -> {
                    String contentId = item.getContentId();
                    String title = tourTitlesMap.getOrDefault(contentId, "장소 이름 없음");

                    Map<String, String> extraInfo = tourExtraInfoMap.getOrDefault(contentId, Collections.emptyMap());
                    String tema = extraInfo.getOrDefault("tema", "");
                    String lDongRegnCd = extraInfo.getOrDefault("lDongRegnCd", "");
                    String lDongSignguCd = extraInfo.getOrDefault("lDongSignguCd", "");

                    String regionKey = lDongRegnCd + "_" + lDongSignguCd;
                    String region = regionNameMap.getOrDefault(regionKey, "");

                    return scheduleItemInfo.builder()
                            .scheduleItemId(item.getScheduleItemId())
                            .contentId(contentId)
                            .title(title)
                            .dayNumber(item.getDayNumber())
                            .memo(item.getMemo())
                            .cost(item.getCost())
                            .order(item.getOrder())
                            .tema(tema)
                            .regionName(region)
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

        List<String> contentIds = items.stream()
                .map(ScheduleItem::getContentId)
                .distinct()
                .collect(Collectors.toList());

        // 위치 정보와 제목 정보를 Tour API 클라이언트를 통해 가져옵니다.
        Map<String, Map<String, Double>> locationMap = tourApiClient.getTourLocationMapByContentIds(contentIds);
        Map<String, String> tourTitlesMap = tourApiClient.getTourTitlesMapByContentIds(contentIds);
        Map<String, TourCategory> tourCategoriesMap = tourApiClient.getTourCategoriesMapByContentIds(contentIds);
        // AiService에 전달할 DTO 리스트를 생성합니다. (title 포함)
        List<AiService.ItemWithLocationInfo> itemsWithLocation = items.stream()
                .map(item -> {
                    String contentId = item.getContentId();
                    Map<String, Double> loc = locationMap.getOrDefault(contentId, Collections.emptyMap());
                    String title = tourTitlesMap.getOrDefault(contentId, "정보 없음");
                    double latitude = loc.getOrDefault("latitude", 0.0);
                    double longitude = loc.getOrDefault("longitude", 0.0);
                    String category = Optional.ofNullable(tourCategoriesMap.get(contentId))
                            .map(Enum::name)
                            .orElse("ETC"); // 기본값 ETC
                    return new AiService.ItemWithLocationInfo(contentId, title, latitude, longitude, category);
                })
                .collect(Collectors.toList());

        String optimizedJson = aiService.getOptimizedRouteJson(
                schedule.getScheduleId(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getStartPlace(),
                schedule.getStartTime(),
                itemsWithLocation
        ).block();

        try {
            // AI 응답을 파싱하여 스케줄 아이템을 업데이트합니다.
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

                items.stream()
                        .filter(item -> item.getContentId().equals(contentId))
                        .findFirst()
                        .ifPresent(originalItem -> {
                            boolean isAccommodation = Optional.ofNullable(tourCategoriesMap.get(contentId))
                                    .map(Enum::name)
                                    .orElse("")
                                    .equals("ACCOMMODATION");

                            ScheduleItem.ScheduleItemBuilder builder = ScheduleItem.builder()
                                    .contentId(originalItem.getContentId())
                                    .memo(originalItem.getMemo())
                                    .cost(originalItem.getCost())
                                    .scheduleId(originalItem.getScheduleId())
                                    .order(order)
                                    .dayNumber(dayNumber);

                            if (isAccommodation) {
                                // 숙소는 여러 날에 반복될 수 있으므로 새로운 PK 발급
                                builder.scheduleItemId(UUID.randomUUID());
                            } else {
                                // 기존 아이템은 업데이트만 (중복 방지)
                                builder.scheduleItemId(originalItem.getScheduleItemId());
                            }

                            updatedItems.add(builder.build());
                        });
            }

            scheduleItemRepository.saveAll(updatedItems);

        } catch (JsonProcessingException e) {
            log.error("AI 응답 JSON 파싱 실패", e);
            throw new RuntimeException("AI 응답 JSON 파싱에 실패했습니다.", e);
        }
    }

    @Transactional
    public void optimizeTestRoute(UUID scheduleId) {
        // [1단계: 모든 데이터 준비]
        UUID currentUserId = getCurrentUserId();
        scheduleFilter.validateScheduleAccess(scheduleId, currentUserId);

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));

        List<ScheduleItem> originalItems = scheduleItemRepository.findAllByScheduleId_ScheduleId(scheduleId);
        if (originalItems.isEmpty()) {
            throw new IllegalArgumentException("해당 스케줄에 아이템이 없습니다.");
        }

        List<String> contentIds = originalItems.stream().map(ScheduleItem::getContentId).distinct().collect(Collectors.toList());
        Map<String, Map<String, Double>> locationMap = tourApiClient.getTourLocationMapByContentIds(contentIds);
        Map<String, TourCategory> tourCategoriesMap = tourApiClient.getTourCategoriesMapByContentIds(contentIds);
        Map<String, Double> startCoordinates = mapService.getCoordinates(schedule.getStartPlace());

        // [2단계: 지능형 DayNumber 할당 (숙소 규칙 적용)]
        List<ScheduleItem> accommodations = new LinkedList<>();
        List<ScheduleItem> others = new LinkedList<>();

        for (ScheduleItem item : originalItems) {
            if (!locationMap.containsKey(item.getContentId())) continue;
            TourCategory category = tourCategoriesMap.get(item.getContentId());
            if (category == TourCategory.ACCOMMODATION) {
                accommodations.add(item);
            } else {
                others.add(item);
            }
        }

        long numberOfDays = ChronoUnit.DAYS.between(schedule.getStartDate(), schedule.getEndDate()) + 1;
        if (numberOfDays <= 0) numberOfDays = 1;

        Map<Integer, List<ScheduleItem>> itemsByDay = new HashMap<>();
        for (int i = 1; i <= numberOfDays; i++) {
            itemsByDay.put(i, new ArrayList<>());
        }

        if (!accommodations.isEmpty()) {
            itemsByDay.get(1).add(accommodations.get(0));
            if (numberOfDays > 1 && accommodations.size() > numberOfDays - 2) {
                itemsByDay.get((int)numberOfDays).add(accommodations.get((int)numberOfDays - 2));
            }
            for (int i = 2; i < numberOfDays; i++) {
                if (accommodations.size() > i - 1) {
                    itemsByDay.get(i).add(accommodations.get(i - 2));
                    itemsByDay.get(i).add(accommodations.get(i - 1));
                }
            }
        }

        int dayIndex = 1;
        for (ScheduleItem item : others) {
            itemsByDay.get(dayIndex).add(item);
            dayIndex = (dayIndex % (int)numberOfDays) + 1;
        }

        // [3단계] GraphHopper 연동: 날짜별 순서(`order`) 최적화
        List<ScheduleItem> finalNewItems = new ArrayList<>();
        Set<UUID> processedItemIds = new HashSet<>();

        for (Map.Entry<Integer, List<ScheduleItem>> entry : itemsByDay.entrySet()) {
            int dayNumber = entry.getKey();
            List<ScheduleItem> itemsForDay = entry.getValue();

            if (itemsForDay.isEmpty()) continue;

            List<MapRequest.Point> points = new ArrayList<>();
            if (dayNumber == 1) {
                points.add(MapRequest.Point.builder()
                        .contentId("START_PLACE")
                        .lat(startCoordinates.get("latitude"))
                        .lon(startCoordinates.get("longitude"))
                        .build());
            }

            itemsForDay.forEach(item -> {
                Map<String, Double> loc = locationMap.get(item.getContentId());
                if (loc != null && loc.get("latitude") != 0.0) {
                    points.add(MapRequest.Point.builder()
                            .contentId(item.getContentId()).lat(loc.get("latitude")).lon(loc.get("longitude")).build());
                }
            });

            if (points.size() < 2) {
                itemsForDay.forEach(item -> {
                    ScheduleItem.ScheduleItemBuilder builder = ScheduleItem.builder()
                            .contentId(item.getContentId())
                            .memo(item.getMemo())
                            .cost(item.getCost())
                            .scheduleId(schedule)
                            .dayNumber(dayNumber)
                            .order(1);

                    if (processedItemIds.contains(item.getScheduleItemId())) {
                        builder.scheduleItemId(UUID.randomUUID());
                    } else {
                        builder.scheduleItemId(item.getScheduleItemId());
                        processedItemIds.add(item.getScheduleItemId());
                    }
                    finalNewItems.add(builder.build());
                });
                continue;
            }

            MapRequest mapRequest = MapRequest.builder().points(points).build();
            List<MapResponse> optimizedResult = mapService.findOptimalRoute(mapRequest);

            for (MapResponse res : optimizedResult) {
                if (res.getContentId().equals("START_PLACE")) continue;

                originalItems.stream()
                        .filter(item -> item.getContentId().equals(res.getContentId()))
                        .findFirst()
                        .ifPresent(originalItem -> {
                            ScheduleItem.ScheduleItemBuilder builder = ScheduleItem.builder()
                                    .contentId(originalItem.getContentId())
                                    .memo(originalItem.getMemo())
                                    .cost(originalItem.getCost())
                                    .scheduleId(originalItem.getScheduleId())
                                    .dayNumber(dayNumber)
                                    .order(res.getOrder());

                            TourCategory category = tourCategoriesMap.get(originalItem.getContentId());
                            if (category == TourCategory.ACCOMMODATION || processedItemIds.contains(originalItem.getScheduleItemId())) {
                                builder.scheduleItemId(UUID.randomUUID());
                            } else {
                                builder.scheduleItemId(originalItem.getScheduleItemId());
                                processedItemIds.add(originalItem.getScheduleItemId());
                            }
                            finalNewItems.add(builder.build());
                        });
            }
        }

        // [4단계] DB 저장
        scheduleItemRepository.deleteAllByScheduleId_ScheduleId(scheduleId);
        scheduleItemRepository.flush();
        scheduleItemRepository.saveAll(finalNewItems);
    }
}
