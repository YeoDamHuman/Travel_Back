package com.example.backend.tour.dto.response;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourDetailResponse {
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
    private String lDongRegnCd;
    private String lDongSignguCd;
    private String lclsSystm1;
    private String lclsSystm2;
    private String lclsSystm3;
}