package com.example.backend.region.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class RegionResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionInfo {
        private Long regionId;
        private String regionName;
        private String regionCode;
        private String lDongRegnCd;
        private String lDongSignguCd;
        private String regionImage;
        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionListResponse {
        private List<RegionInfo> regions;
        private int totalCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotRegionInfo {
        private Long regionId;
        private String regionName;
        private String regionCode;
        private String lDongRegnCd;
        private String lDongSignguCd;
        private String regionImage;
        private String description;
        private Long viewCount;
        private LocalDateTime lastViewedAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotRegionListResponse {
        private List<HotRegionInfo> hotRegions;
        private int totalCount;
        private LocalDateTime generatedAt;
    }
}