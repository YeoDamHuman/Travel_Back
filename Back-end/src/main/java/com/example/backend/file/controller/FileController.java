package com.example.backend.file.controller;

import com.cloudinary.Cloudinary;
import com.example.backend.file.dto.request.FileRequest;
import com.example.backend.file.dto.response.FileResponse;
import com.example.backend.file.service.CloudinaryUploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

    private final CloudinaryUploader uploader;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "이미지 업로드", description = "프로필 이미지 업로드 API")
    public ResponseEntity<FileResponse.uploadResponse> upload(
            @Parameter(description = "업로드할 이미지 파일", required = true)
            @RequestPart("image") MultipartFile image) {

        String imageUrl = uploader.uploadImage(image, "profile-images");
        return ResponseEntity.ok(new FileResponse.uploadResponse(imageUrl));
    }
}
