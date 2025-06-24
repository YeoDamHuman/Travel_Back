package com.example.backend.file.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

public class FileRequest {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class uploadRequest {
        @Schema(description = "업로드할 이미지 파일", type = "string", format = "binary", required = true)
        private MultipartFile image;

    }
}
