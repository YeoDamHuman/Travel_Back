package com.example.backend.cart.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.example.backend.tour.entity.TourCategory;

public class CartResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartDetailResponse {
        private UUID cartId;
        private String region;
        private List<TourInfo> tours;
        private int totalCount;
        private long totalPrice;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourInfo {
        private UUID tourId;
        private Double longitude;
        private Double latitude;
        private String address;
        private String image;
        private String tema;
        private TourCategory category;
        private Long price;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddTourResponse {
        private UUID tourId;
        private String message;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourSearchResponse {
        private String contentId;
        private String contentTypeId;
        private String title;
        private String address;
        private String firstImage;
        private String firstImage2;
        private String mapX;
        private String mapY;
        private String areaCode;
        private String sigunguCode;
        private String tel;
        private String overview;
    }
}