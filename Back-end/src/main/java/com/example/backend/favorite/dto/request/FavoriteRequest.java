package com.example.backend.favorite.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FavoriteRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddFavoriteRequest {
        private String contentId;
    }
}