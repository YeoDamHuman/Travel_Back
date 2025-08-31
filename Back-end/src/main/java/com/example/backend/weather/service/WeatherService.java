package com.example.backend.weather.service;

import com.example.backend.weather.converter.City;
import com.example.backend.weather.dto.response.WeatherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    public Mono<WeatherResponse.weatherDataResponse> getWeatherByCity(String city) {
        // 2. Enum의 정적 메서드를 호출하여 한글 도시 이름을 영어로 변환합니다.
        String englishCity = City.findEnglishNameByKoreanName(city);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.openweathermap.org")
                        .path(apiUrl)
                        .queryParam("q", englishCity) // 3. 변환된 영어 도시 이름으로 API를 요청합니다.
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .queryParam("lang", "kr")
                        .build()
                )
                .retrieve()
                .bodyToMono(WeatherResponse.weatherDataResponse.class);
    }
}