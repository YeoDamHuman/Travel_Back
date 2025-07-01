package com.example.backend.weather.controller;

import com.example.backend.weather.dto.response.WeatherResponse;
import com.example.backend.weather.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/weather")
@RequiredArgsConstructor
@Tag(name = "WeatherAPI", description = "날씨 관련 API.")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/current")
    @Operation(summary = "도시별 현재 날씨 조회",
            description = "도시 이름을 입력 받아 OpenWeatherMap API에서 현재 날씨 정보를 조회합니다.")
    public Mono<WeatherResponse.weatherDataResponse> getWeatherByCity(@RequestParam String city) {
        return weatherService.getWeatherByCity(city);
    }
}
