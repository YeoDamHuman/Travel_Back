package com.example.backend.map.dto.request;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * GraphHopper 경로 최적화를 요청하기 위한 DTO입니다.
 * 최적화할 경유지(Point)의 목록을 담고 있습니다.
 */
@Getter
@Builder // 👈 @Builder 어노테이션 추가
public class MapRequest {

    private final List<Point> points;

    /**
     * 개별 경유지의 위도(latitude)와 경도(longitude)를 나타내는 내부 클래스입니다.
     */
    @Getter
    @Builder
    public static class Point {
        private final String contentId;
        private final double lat; // 위도
        private final double lon; // 경도
    }
}