package com.example.backend.cart.dto.request;

import lombok.*;
import java.math.BigDecimal;

public class CartRequest {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddTourRequest {
        private BigDecimal longitude;
        private BigDecimal latitude;
        private String address;
        private String image;
        private String tema;
    }
}