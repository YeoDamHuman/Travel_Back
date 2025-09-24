package com.example.backend.schedule.service;

import com.example.backend.schedule.dto.request.RouteOptimizerRequest;
import com.example.backend.schedule.dto.response.RouteOptimizerResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteOptimizerService {

    private final ObjectMapper objectMapper;

    /**
     * AI가 생성한 일자별 계획을 받아, 각 날짜 내에서 동선을 최적화하고 최종 스케줄을 생성합니다.
     * @param dailyPlanJson AiService가 생성한 JSON 문자열
     * @param startPlace 1일차의 최초 출발지 정보
     * @return 최종적으로 순서가 결정된 스케줄 객체
     */
    public RouteOptimizerResponse optimizeRoute(String dailyPlanJson, RouteOptimizerRequest.PlaceInfo startPlace) throws IOException {
        RouteOptimizerRequest requestDto = objectMapper.readValue(dailyPlanJson, RouteOptimizerRequest.class);
        List<RouteOptimizerResponse.OptimizedScheduleItem> finalItems = new ArrayList<>();
        int totalDays = requestDto.getDailyPlans().size();

        RouteOptimizerRequest.PlaceInfo currentStartPlace = startPlace;

        for (RouteOptimizerRequest.DailyPlan day : requestDto.getDailyPlans()) {
            log.info("▶️ {}일차 동선 최적화 시작...", day.getDayNumber());

            final RouteOptimizerRequest.PlaceInfo startNodeForThisDay = currentStartPlace;

            List<RouteOptimizerRequest.PlaceInfo> placesToVisit = new ArrayList<>(day.getItems());
            List<RouteOptimizerRequest.PlaceInfo> optimizedOrder = new ArrayList<>();

            RouteOptimizerRequest.PlaceInfo endAccommodation = null;
            if (day.getDayNumber() < totalDays) {
                Optional<RouteOptimizerRequest.PlaceInfo> accommodationOpt = placesToVisit.stream()
                        .filter(p -> "ACCOMMODATION".equals(p.getCategory()) && !p.equals(startNodeForThisDay))
                        .findFirst();
                if (accommodationOpt.isPresent()) {
                    endAccommodation = accommodationOpt.get();
                    placesToVisit.remove(endAccommodation);
                    log.info("  📌 {}일차 도착 숙소 고정: {}", day.getDayNumber(), endAccommodation.getTitle());
                }
            }

            if (day.getDayNumber() > 1) {
                placesToVisit.remove(startNodeForThisDay);
            }

            RouteOptimizerRequest.PlaceInfo currentLocation = startNodeForThisDay;
            while (!placesToVisit.isEmpty()) {
                RouteOptimizerRequest.PlaceInfo nearest = findNearest(currentLocation, placesToVisit);
                optimizedOrder.add(nearest);
                placesToVisit.remove(nearest);
                currentLocation = nearest;
            }

            if (endAccommodation != null) {
                optimizedOrder.add(endAccommodation);
            }

            List<RouteOptimizerRequest.PlaceInfo> finalOrderForDay = new ArrayList<>();
            if (day.getDayNumber() > 1) {
                finalOrderForDay.add(startNodeForThisDay);
            }
            finalOrderForDay.addAll(optimizedOrder);


            for (int i = 0; i < finalOrderForDay.size(); i++) {
                RouteOptimizerRequest.PlaceInfo item = finalOrderForDay.get(i);
                finalItems.add(new RouteOptimizerResponse.OptimizedScheduleItem(i + 1, item.getContentId(), day.getDayNumber()));
            }

            // 루프의 마지막에서 다음 루프를 위해 currentStartPlace 값을 업데이트하는 것은 그대로 유지
            currentStartPlace = finalOrderForDay.get(finalOrderForDay.size() - 1);
            log.info("✅ {}일차 동선 최적화 완료! 다음 날 시작점: {}", day.getDayNumber(), currentStartPlace.getTitle());
        }

        return new RouteOptimizerResponse(requestDto.getScheduleId(), finalItems);
    }


    /**
     * 현재 위치에서 가장 가까운 장소를 찾습니다.
     */
    private RouteOptimizerRequest.PlaceInfo findNearest(RouteOptimizerRequest.PlaceInfo from, List<RouteOptimizerRequest.PlaceInfo> candidates) {
        if (candidates.isEmpty()) {
            return null;
        }

        RouteOptimizerRequest.PlaceInfo nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (RouteOptimizerRequest.PlaceInfo candidate : candidates) {
            double distance = calculateDistance(from.getLatitude(), from.getLongitude(), candidate.getLatitude(), candidate.getLongitude());

            if (distance < minDistance) {
                minDistance = distance;
                nearest = candidate;
            }
        }
        return nearest;
    }

    /**
     * 두 지점 간의 직선 거리(Haversine 공식)를 계산합니다.
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // 지구 반지름 (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}