package com.example.backend.schedule.dto.request;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
public class ScheduleOptimizeRequest {
    private String optimizationType; // distance
    private OptimizationPreferencse preferencse; // 중첩된 preferences 객체

    @Getter
    @Builder
    @AllArgsConstructor
    public static class OptimizationPreferencse {
        private String transportationType; // "car"
        private boolean avoidTolls;        // false
        private boolean includeRestStops;  // true
    }
}
