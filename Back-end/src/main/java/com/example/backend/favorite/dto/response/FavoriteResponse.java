package com.example.backend.favorite.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class FavoriteResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteInfo {
        private UUID favoriteId;
        private String contentId;
        private String placeTitle;
        private String placeAddress;
        private String placeImage;
        private String regionCode;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteListResponse {
        private List<FavoriteInfo> favorites;
        private int totalCount;
    }

    @Getter
    @AllArgsConstructor
    public static class FavoriteActionResponse {
        private String message;
        private boolean isFavorite;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteRegionInfo {
        private String contentId;
        private String placeTitle;
        private String placeImage;
        private String tema;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FavoriteRegionResponse {
        private List<FavoriteRegionInfo> favorites;
        private String lDongRegnCd;
        private String lDongSignguCd;
        private int totalCount;
    }
}