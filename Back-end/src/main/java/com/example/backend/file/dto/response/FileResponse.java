package com.example.backend.file.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class FileResponse {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class uploadResponse {
        private String imageUrl;
    }
}
