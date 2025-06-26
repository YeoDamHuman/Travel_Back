package com.example.backend.cart.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class CartRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddTourRequest {
        private BigDecimal longitude;
        private BigDecimal latitude;
        private String address;
        private String image;
        private String tema;
    }
}