package com.example.backend.weather.service;

import com.example.backend.weather.converter.City;
import com.example.backend.weather.dto.response.WeatherResponse;
import com.example.backend.weather.dto.response.WeatherResponse.weatherDataResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;
    /**
     * One Call API를 호출하여 현재, 최저, 최고 기온을 포함한 날씨 정보를 DTO 형태로 반환합니다.
     * @param cityName 한글 도시 이름 (예: "서울")
     * @return 가공된 날씨 정보가 담긴 Mono<weatherDataResponse>
     */
    public Mono<weatherDataResponse> getCurrentAndDailyWeather(String cityName) {
        // Enum에서 도시 정보를 찾습니다.
        City city = City.findByKoreanName(cityName)
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 도시입니다: " + cityName));

        // 찾은 도시의 위도(lat), 경도(lon)를 사용해 API를 호출합니다.
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.openweathermap.org")
                        .path(apiUrl) // One Call API 엔드포인트를 직접 지정합니다.
                        .queryParam("lat", city.getLat())
                        .queryParam("lon", city.getLon())
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .queryParam("lang", "kr")
                        .queryParam("exclude", "minutely,alerts")
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .map(responseBody -> {
                    try {
                        // API 응답을 파싱하여 원하는 DTO 형태로 가공합니다.
                        JsonNode root = objectMapper.readTree(responseBody);

                        WeatherResponse.Info info = WeatherResponse.Info.builder()
                                .temp(root.path("current").path("temp").asDouble())
                                .humidity(root.path("current").path("humidity").asInt())
                                .temp_min(root.path("daily").get(0).path("temp").path("min").asDouble())
                                .temp_max(root.path("daily").get(0).path("temp").path("max").asDouble())
                                .build();

                        JsonNode weatherNode = root.path("current").path("weather").get(0);
                        WeatherResponse.Weather[] weather = new WeatherResponse.Weather[]{
                                WeatherResponse.Weather.builder()
                                        .main(weatherNode.path("main").asText())
                                        .description(weatherNode.path("description").asText())
                                        .icon(weatherNode.path("icon").asText())
                                        .build()
                        };

                        return weatherDataResponse.builder()
                                .main(info)
                                .weather(weather)
                                .name(city.getKoreanName())
                                .build();

                    } catch (Exception e) {
                        throw new RuntimeException("날씨 데이터 파싱에 실패했습니다.", e);
                    }
                });
    }
}