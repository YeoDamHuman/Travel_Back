package com.example.backend.map.service;

import com.example.backend.map.dto.request.MapRequest;
import com.example.backend.map.dto.request.MapRequest.Point;
import com.example.backend.map.dto.response.MapResponse;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPoint3D;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MapService {

    // --- 의존성 ---
    private final WebClient.Builder webClientBuilder;
    private final GraphHopper graphHopper;

    // --- 카카오 API 관련 필드 ---
    @Value("${kakao.url}")
    private String apiUrl;
    @Value("${kakao.restApiKey}")
    private String apiKey;
    private WebClient webClient;

    @PostConstruct
    public void init() {
        String kakaoApiKey = "KakaoAK " + apiKey;
        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, kakaoApiKey)
                .build();
    }

    /**
     * [카카오 API] 키워드를 위도/경도 좌표로 변환합니다. (지오코딩)
     */
    public Map<String, Double> getCoordinates(String keyword) {
        Map<String, Object> response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", keyword)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response == null || ((List) response.get("documents")).isEmpty()) {
            throw new IllegalArgumentException("키워드에 해당하는 좌표를 찾을 수 없습니다: " + keyword);
        }

        Map<String, Object> document = ((List<Map<String, Object>>) response.get("documents")).get(0);
        double latitude = Double.parseDouble(document.get("y").toString());
        double longitude = Double.parseDouble(document.get("x").toString());

        return Map.of("latitude", latitude, "longitude", longitude);
    }

    /**
     * [GraphHopper] 좌표 목록을 받아 최적의 방문 순서를 계산합니다.
     *
     * @param request 최적화할 경유지 목록이 담긴 DTO
     * @return 최적화된 순서로 정렬된 경유지(Point) 목록
     */
    public List<MapResponse> findOptimalRoute(MapRequest request) {
        List<MapRequest.Point> originalPoints = request.getPoints();
        if (originalPoints == null || originalPoints.size() < 2) {
            // 경로 최적화를 할 수 없는 경우, 받은 순서 그대로 order만 붙여서 반환
            List<MapResponse> result = new ArrayList<>();
            for (int i = 0; i < originalPoints.size(); i++) {
                MapRequest.Point p = originalPoints.get(i);
                result.add(MapResponse.builder()
                        .order(i + 1).contentId(p.getContentId()).lat(p.getLat()).lon(p.getLon()).build());
            }
            return result;
        }

        GHRequest ghRequest = new GHRequest();
        originalPoints.forEach(p -> ghRequest.addPoint(new GHPoint(p.getLat(), p.getLon())));

        ghRequest.setProfile("car");
        ghRequest.setAlgorithm("round_trip");
        ghRequest.getHints().putObject("round_trip.seed", "0");

        GHResponse response = graphHopper.route(ghRequest);
        if (response.hasErrors()) {
            throw new RuntimeException("경로 최적화 중 오류 발생: " + response.getErrors());
        }

        ResponsePath bestPath = response.getBest();

        // 1. 최적화된 '경유지 좌표' 목록(PointList)을 가져옵니다.
        PointList optimizedWaypoints = bestPath.getWaypoints();

        // 2. 최적화된 좌표 순서에 따라 원본 Point 리스트를 재정렬합니다.
        List<MapRequest.Point> sortedOriginalPoints = new ArrayList<>();
        for (GHPoint3D optimizedPoint : optimizedWaypoints) {
            originalPoints.stream()
                    .filter(p -> Math.abs(p.getLat() - optimizedPoint.getLat()) < 1e-6 &&
                            Math.abs(p.getLon() - optimizedPoint.getLon()) < 1e-6)
                    .findFirst()
                    .ifPresent(sortedOriginalPoints::add);
        }

        // 3. 최종 결과를 새로운 응답 DTO 리스트로 만듭니다.
        List<MapResponse> finalResult = new ArrayList<>();
        for (int i = 0; i < sortedOriginalPoints.size(); i++) {
            MapRequest.Point currentPoint = sortedOriginalPoints.get(i);
            finalResult.add(
                    MapResponse.builder()
                            .order(i + 1)
                            .contentId(currentPoint.getContentId())
                            .lat(currentPoint.getLat())
                            .lon(currentPoint.getLon())
                            .build()
            );
        }

        return finalResult;
    }
}