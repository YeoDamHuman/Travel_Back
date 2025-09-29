package com.example.backend.file.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Cloudinary 이미지 업로드를 처리하는 서비스 클래스입니다.
 * MultipartFile 또는 URL로부터 이미지를 업로드하는 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class CloudinaryUploader {

    private final Cloudinary cloudinary;

    /**
     * MultipartFile 형식의 이미지를 Cloudinary에 업로드하고, 보안 URL을 반환합니다.
     * @param file 업로드할 파일 (MultipartFile)
     * @param folder Cloudinary에 저장할 폴더 이름
     * @return 업로드된 이미지의 영구적인 secure_url
     */
    public String uploadImage(MultipartFile file, String folder) {
        try {
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder)
            );
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Cloudinary 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 이미지 URL로부터 이미지를 다운로드하여 Cloudinary에 업로드하고, 보안 URL을 반환합니다.
     * @param imageUrl 다운로드 및 업로드할 이미지의 URL
     * @param folder Cloudinary에 저장할 폴더 이름
     * @return 업로드된 이미지의 영구적인 secure_url
     */
    public String uploadImageFromUrl(String imageUrl, String folder) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.getForEntity(imageUrl, byte[].class);
            byte[] imageBytes = response.getBody();

            if (imageBytes == null) {
                throw new IOException("URL로부터 이미지를 다운로드할 수 없습니다: " + imageUrl);
            }

            Map uploadResult = cloudinary.uploader().upload(
                    imageBytes,
                    ObjectUtils.asMap("folder", folder)
            );
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("URL로부터 이미지 업로드에 실패했습니다.", e);
        }
    }
}