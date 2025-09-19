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
        // 1. AI가 만든 JSON을 Request DTO 객체로 변환
        RouteOptimizerRequest requestDto = objectMapper.readValue(dailyPlanJson, RouteOptimizerRequest.class);
        List<RouteOptimizerResponse.OptimizedScheduleItem> finalItems = new ArrayList<>();

        // 2. 일자별로 루프를 돌며 동선 최적화 수행
        RouteOptimizerRequest.PlaceInfo currentStartPlace = startPlace; // 루프의 시작점

        for (RouteOptimizerRequest.DailyPlan day : requestDto.getDailyPlans()) {
            log.info("▶️ {}일차 동선 최적화 시작...", day.getDayNumber());

            List<RouteOptimizerRequest.PlaceInfo> placesToVisit = new ArrayList<>(day.getItems());
            List<RouteOptimizerRequest.PlaceInfo> optimizedOrder = new ArrayList<>();

            // 3. 해당일의 장소들을 Nearest Neighbor 알고리즘으로 정렬
            RouteOptimizerRequest.PlaceInfo currentLocation = currentStartPlace;

            while (!placesToVisit.isEmpty()) {
                RouteOptimizerRequest.PlaceInfo nearest = findNearest(currentLocation, placesToVisit);
                optimizedOrder.add(nearest);
                placesToVisit.remove(nearest);
                currentLocation = nearest;
            }

            // 4. 최적화된 순서대로 Response DTO 포맷에 추가
            for (int i = 0; i < optimizedOrder.size(); i++) {
                RouteOptimizerRequest.PlaceInfo item = optimizedOrder.get(i);
                finalItems.add(new RouteOptimizerResponse.OptimizedScheduleItem(i + 1, item.getContentId(), day.getDayNumber()));
            }

            // 5. 다음 날의 시작점을 현재 날짜의 마지막 장소(숙소)로 업데이트
            currentStartPlace = optimizedOrder.get(optimizedOrder.size() - 1);
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