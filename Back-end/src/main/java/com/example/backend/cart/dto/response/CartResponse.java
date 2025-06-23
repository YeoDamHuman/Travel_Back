package com.example.backend.cart.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CartResponse {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartDetailResponse {
        private UUID cartId;
        private String region;
        private List<TourDetailResponse> tours;
        private Integer totalCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourDetailResponse {
        private UUID tourId;
        private BigDecimal longitude;
        private BigDecimal latitude;
        private String address;
        private String image;
        private String tema;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddTourResponse {
        private UUID tourId;
        private String message;
    }

    /**
     * KorService1 API 응답에 맞춘 검색 응답 DTO
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourSearchResponse {
        // 기본 정보
        private String contentId;          // 콘텐츠 ID
        private String contentTypeId;      // 관광타입 ID
        private String title;              // 제목 (기존 rlteTatsNm → title)

        // 주소 정보
        private String address;            // 주소 (addr1)
        private String address2;           // 상세주소 (addr2)
        private String areaCode;           // 지역코드
        private String sigunguCode;        // 시군구코드

        // 위치 정보
        private double latitude;           // 위도 (mapy)
        private double longitude;          // 경도 (mapx)

        // 이미지 정보
        private String image;              // 대표이미지 (firstimage)
        private String thumbnail;          // 썸네일이미지 (firstimage2)

        // 기타 정보
        private String tel;                // 전화번호
        private String createdTime;        // 생성시간
        private String modifiedTime;       // 수정시간

        // 추가 필드 (필요시)
        private String tema;               // 테마/카테고리 정보
        private String description;        // 설명
    }
}