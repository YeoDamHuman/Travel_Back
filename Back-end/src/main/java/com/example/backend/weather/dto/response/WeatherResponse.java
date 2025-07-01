package com.example.backend.weather.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class WeatherResponse {

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "날씨 데이터 응답")
    public static class weatherDataResponse {
        @Schema(description = "기본 정보", implementation = Info.class)
        private Info main;
        @Schema(description = "날씨 상태 배열", implementation = Weather.class)
        private Weather[] weather;
        @Schema(description = "도시 이름", example = "Seoul")
        private String name;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "날씨 상태 정보")
    public static class Weather {
        @Schema(description = "날씨 상태 메인", example = "Clear")
        private String main;
        @Schema(description = "상세 설명", example = "clear sky")
        private String description;
        @Schema(description = "아이콘 코드", example = "01d")
        private String icon;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @Schema(description = "기본 기상 정보")
    public static class Info {
        @Schema(description = "현재 온도", example = "25.5")
        private double temp;
        @Schema(description = "습도(%)", example = "60")
        private int humidity;
        @Schema(description = "최저 온도", example = "20.0")
        private double temp_min;
        @Schema(description = "최고 온도", example = "28.0")
        private double temp_max;
    }
}