package com.example.backend.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

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

        public static CartDetailResponse empty() {
            return new CartDetailResponse(null, "", List.of(), 0, 0L);
        }
    }

    @Getter
    @Builder
    public static class TourInfo {
        private UUID tourId;
        private Double longitude;
        private Double latitude;
        private String address;
        private String image;
        private String tema;
        private String category;
        private Long price;
    }

    @Getter
    @AllArgsConstructor
    public static class AddTourResponse {
        private UUID tourId;
        private String message;
    }
}
