package com.example.backend.schedule.service;

import com.example.backend.common.auth.AuthUtil;
import com.example.backend.region.service.RegionService;
import com.example.backend.region.service.RegionService.CodePair;
import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleCreateRequest;
import com.example.backend.schedule.dto.request.ScheduleRequest.ScheduleUpdateRequest;
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleDetailResponse;
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleInfo;
import com.example.backend.schedule.dto.response.ScheduleResponse.scheduleItemInfo;
import com.example.backend.scheduleItem.service.ScheduleItemService;
import com.example.backend.schedule.entity.Schedule;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
    private final ScheduleItemService scheduleItemService;
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    private final TourApiClient tourApiClient;
    private final RegionService regionService;

    /**
     * 새로운 스케줄을 생성하고 스케줄 아이템들을 저장합니다.
     * 요청한 사용자가 스케줄의 첫 번째 참여자가 됩니다.
     *
     * @param request 생성할 스케줄의 상세 정보가 담긴 {@link ScheduleCreateRequest} 객체.
     * @return 새로 생성된 스케줄의 ID.
     */
    @Transactional
    public UUID createSchedule(ScheduleCreateRequest request) {
        User currentUser = AuthUtil.getCurrentUser(userRepository);

        Schedule newSchedule = ScheduleCreateRequest.toEntity(request);
        newSchedule.getUsers().add(currentUser);
        Schedule savedSchedule = scheduleRepository.save(newSchedule);

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
     * 기존 스케줄의 정보를 업데이트합니다.
     * 스케줄에 참여한 사용자만 수정할 수 있습니다.
     *
     * @param request 업데이트할 스케줄의 ID와 새로운 정보가 담긴 {@link ScheduleUpdateRequest} 객체.
     * @return 업데이트된 스케줄의 ID.
     */
    @Transactional
    public UUID updateSchedule(ScheduleUpdateRequest request) {
        User currentUser = AuthUtil.getCurrentUser(userRepository);
        Schedule originalSchedule = scheduleRepository.findWithUsersById(request.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("수정하려는 스케줄을 찾을 수 없습니다."));

        if (originalSchedule.getUsers().stream().noneMatch(user -> user.equals(currentUser))) {
            throw new AccessDeniedException("스케줄을 수정할 권한이 없습니다.");
        }

        Schedule updatedSchedule = ScheduleUpdateRequest.toEntity(request, originalSchedule);
        scheduleRepository.save(updatedSchedule);

        return updatedSchedule.getScheduleId();
    }

    /**
     * 주어진 ID를 가진 스케줄을 삭제합니다.
     * 스케줄에 참여한 사용자만 삭제할 수 있습니다.
     *
     * @param scheduleId 삭제할 스케줄의 ID.
     */
    @Transactional
    public void deleteSchedule(UUID scheduleId) {
        User currentUser = AuthUtil.getCurrentUser(userRepository);
        Schedule schedule = scheduleRepository.findWithUsersById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("삭제하려는 스케줄을 찾을 수 없습니다."));

        if (schedule.getUsers().stream().noneMatch(user -> user.equals(currentUser))) {
            throw new AccessDeniedException("스케줄을 삭제할 권한이 없습니다.");
        }

        scheduleItemRepository.deleteAllByScheduleId_ScheduleId(scheduleId);
        scheduleRepository.deleteById(scheduleId);
    }

    /**
     * 현재 사용자가 참여하고 있는 스케줄 목록을 조회합니다.
     *
     * @return 조회된 스케줄 정보 목록.
     */
    @Transactional(readOnly = true)
    public List<scheduleInfo> getSchedules() {
        User currentUser = AuthUtil.getCurrentUser(userRepository);
        List<Schedule> schedules = scheduleRepository.findAllByUsersContaining(currentUser);

        if (schedules.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> scheduleIds = schedules.stream().map(Schedule::getScheduleId).collect(Collectors.toList());
        List<ScheduleItem> firstItems = scheduleItemRepository.findFirstItemForEachSchedule(scheduleIds);
        Map<UUID, String> scheduleToContentIdMap = firstItems.stream()
                .collect(Collectors.toMap(
                        item -> item.getScheduleId().getScheduleId(),
                        ScheduleItem::getContentId,
                        (existing, replacement) -> existing
                ));
        List<String> representativeContentIds = new ArrayList<>(scheduleToContentIdMap.values());
        Map<String, Map<String, String>> tourExtraInfoMap = tourApiClient.getTourExtraInfoMapByContentIds(representativeContentIds);
        List<CodePair> codePairs = tourExtraInfoMap.values().stream()
                .map(info -> new CodePair(info.get("lDongRegnCd"), info.get("lDongSignguCd")))
                .filter(pair -> pair.lDongRegnCd() != null && pair.lDongSignguCd() != null)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> regionImageMap = regionService.getRegionImagesByCodePairs(codePairs);

        return schedules.stream()
                .map(schedule -> {
                    String contentId = scheduleToContentIdMap.get(schedule.getScheduleId());
                    String regionImage = null;

                    if (contentId != null) {
                        Map<String, String> extraInfo = tourExtraInfoMap.get(contentId);
                        if (extraInfo != null) {
                            String lDongRegnCd = extraInfo.get("lDongRegnCd");
                            String lDongSignguCd = extraInfo.get("lDongSignguCd");
                            if (lDongRegnCd != null && lDongSignguCd != null) {
                                String key = lDongRegnCd + "_" + lDongSignguCd;
                                regionImage = regionImageMap.get(key);
                            }
                        }
                    }

                    return scheduleInfo.builder()
                            .scheduleId(schedule.getScheduleId())
                            .scheduleName(schedule.getScheduleName())
                            .startDate(schedule.getStartDate())
                            .endDate(schedule.getEndDate())
                            .createdAt(schedule.getCreatedAt())
                            .updatedAt(schedule.getUpdatedAt())
                            .budget(schedule.getBudget())
                            .scheduleStyle(schedule.getScheduleStyle())
                            .isBoarded(schedule.isBoarded())
                            .regionImage(regionImage)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 주어진 ID를 가진 스케줄의 상세 정보를 조회합니다.
     * 스케줄에 참여한 사용자만 조회할 수 있습니다.
     *
     * @param scheduleId 상세 정보를 조회할 스케줄의 ID.
     * @return 스케줄의 상세 정보와 포함된 스케줄 아이템 목록이 담긴 {@link scheduleDetailResponse} 객체.
     */
    @Transactional(readOnly = true)
    public scheduleDetailResponse getScheduleDetail(UUID scheduleId) {
        User currentUser = AuthUtil.getCurrentUser(userRepository);
        Schedule schedule = scheduleRepository.findWithUsersById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));

        if (schedule.getUsers().stream().noneMatch(user -> user.equals(currentUser))) {
            throw new AccessDeniedException("스케줄을 조회할 권한이 없습니다.");
        }

        List<ScheduleItem> scheduleItems = scheduleItemRepository.findAllByScheduleId_ScheduleId(scheduleId);
        List<String> contentIds = scheduleItems.stream()
                .map(ScheduleItem::getContentId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<String, String> tourTitlesMap = tourApiClient.getTourTitlesMapByContentIds(contentIds);
        Map<String, Map<String, String>> tourExtraInfoMap = tourApiClient.getTourExtraInfoMapByContentIds(contentIds);
        Map<String, Map<String, Double>> tourLocationMap = tourApiClient.getTourLocationMapByContentIds(contentIds);

        List<CodePair> codePairsToSearch = tourExtraInfoMap.values().stream()
                .map(info -> new CodePair(info.get("lDongRegnCd"), info.get("lDongSignguCd")))
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
                    String address = extraInfo.getOrDefault("address", "주소 정보 없음");
                    String regionKey = lDongRegnCd + "_" + lDongSignguCd;
                    String region = regionNameMap.getOrDefault(regionKey, "");
                    Map<String, Double> location = tourLocationMap.getOrDefault(contentId, Collections.emptyMap());
                    Double latitude = location.get("latitude");
                    Double longitude = location.get("longitude");

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
                            .latitude(latitude)
                            .longitude(longitude)
                            .address(address)
                            .lDongRegnCd(lDongRegnCd)
                            .lDongSignguCd(lDongSignguCd)
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
     * 스케줄에 참여한 사용자만 경로를 최적화할 수 있습니다.
     *
     * @param scheduleId 최적화할 스케줄의 ID.
     */
    @Transactional
    public void optimizeRoute(UUID scheduleId) {
        User currentUser = AuthUtil.getCurrentUser(userRepository);
        Schedule schedule = scheduleRepository.findWithUsersById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));

        if (schedule.getUsers().stream().noneMatch(user -> user.equals(currentUser))) {
            throw new AccessDeniedException("스케줄을 최적화할 권한이 없습니다.");
        }

        List<ScheduleItem> items = scheduleItemRepository.findAllByScheduleId_ScheduleId(scheduleId);

        if (items.isEmpty()) {
            throw new IllegalArgumentException("해당 스케줄에 아이템이 없습니다.");
        }

        List<String> contentIds = items.stream()
                .map(ScheduleItem::getContentId)
                .distinct()
                .collect(Collectors.toList());

        Map<String, Map<String, Double>> locationMap = tourApiClient.getTourLocationMapByContentIds(contentIds);
        Map<String, String> tourTitlesMap = tourApiClient.getTourTitlesMapByContentIds(contentIds);
        Map<String, TourCategory> tourCategoriesMap = tourApiClient.getTourCategoriesMapByContentIds(contentIds);

        List<AiService.ItemWithLocationInfo> itemsWithLocation = items.stream()
                .map(item -> {
                    String contentId = item.getContentId();
                    Map<String, Double> loc = locationMap.getOrDefault(contentId, Collections.emptyMap());
                    String title = tourTitlesMap.getOrDefault(contentId, "정보 없음");
                    double latitude = loc.getOrDefault("latitude", 0.0);
                    double longitude = loc.getOrDefault("longitude", 0.0);
                    String category = Optional.ofNullable(tourCategoriesMap.get(contentId))
                            .map(Enum::name)
                            .orElse("ETC");
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
                                builder.scheduleItemId(UUID.randomUUID());
                            } else {
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

    /**
     * 주어진 ID를 가진 스케줄의 상세 정보를 권한 확인 없이 조회합니다.
     *
     * @param scheduleId 상세 정보를 조회할 스케줄의 ID.
     * @return 스케줄의 상세 정보와 포함된 스케줄 아이템 목록이 담긴 {@link scheduleDetailResponse} 객체.
     */
    @Transactional(readOnly = true)
    public scheduleDetailResponse getPublicScheduleDetail(UUID scheduleId) {
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
        Map<String, Map<String, Double>> tourLocationMap = tourApiClient.getTourLocationMapByContentIds(contentIds);

        List<CodePair> codePairsToSearch = tourExtraInfoMap.values().stream()
                .map(info -> new CodePair(info.get("lDongRegnCd"), info.get("lDongSignguCd")))
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
                    String address = extraInfo.getOrDefault("address", "주소 정보 없음");
                    String regionKey = lDongRegnCd + "_" + lDongSignguCd;
                    String region = regionNameMap.getOrDefault(regionKey, "");
                    Map<String, Double> location = tourLocationMap.getOrDefault(contentId, Collections.emptyMap());
                    Double latitude = location.get("latitude");
                    Double longitude = location.get("longitude");

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
                            .latitude(latitude)
                            .longitude(longitude)
                            .address(address)
                            .lDongRegnCd(lDongRegnCd)
                            .lDongSignguCd(lDongSignguCd)
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
     * 현재 사용자를 특정 스케줄의 참여자로 추가합니다.
     *
     * @param scheduleId 참여할 스케줄의 ID
     * @throws IllegalArgumentException 스케줄을 찾을 수 없는 경우
     * @throws IllegalStateException    사용자가 이미 해당 스케줄에 참여하고 있는 경우
     */
    @Transactional
    public void joinSchedule(UUID scheduleId) {
        // 1. 요청을 보낸 사용자를 식별합니다. (JWT 헤더 기반)
        User currentUser = AuthUtil.getCurrentUser(userRepository);

        // 2. 참여하려는 스케줄을 조회합니다.
        Schedule schedule = scheduleRepository.findWithUsersById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄을 찾을 수 없습니다."));

        // 3. 이미 참여자인지 확인하여 중복 추가를 방지합니다.
        if (schedule.getUsers().contains(currentUser)) {
            throw new IllegalStateException("이미 해당 스케줄에 참여하고 있습니다.");
        }

        // 4. 스케줄의 참여자 목록에 현재 사용자를 추가합니다.
        schedule.getUsers().add(currentUser);
    }
}