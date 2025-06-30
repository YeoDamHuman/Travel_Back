package com.example.backend.cart.dto.response;

import lombok.*;
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
        private java.util.List<TourInfo> tours;
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
        private String address2;
        private String zipcode;
        private String areaCode;
        private String cat1;
        private String cat2;
        private String cat3;
        private String createdTime;
        private String firstImage;
        private String firstImage2;
        private String cpyrhtDivCd;
        private String mapX;
        private String mapY;
        private String mlevel;
        private String modifiedTime;
        private String sigunguCode;
        private String tel;
        private String overview;
        private String lDongRegnCd;
        private String lDongSignguCd;
        private String lclsSystm1;
        private String lclsSystm2;
        private String lclsSystm3;
    }
}