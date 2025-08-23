package com.example.backend.tour.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TourResponse {
    private String contentId;
    private String contentTypeId;
    private String title;
    @JsonProperty("addr1")
    private String address;
    @JsonProperty("addr2")
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
    @JsonProperty("lDongRegnCd")
    private String lDongRegnCd;
    @JsonProperty("lDongSignguCd")
    private String lDongSignguCd;
    @JsonProperty("lclsSystm1")
    private String lclsSystm1;
    @JsonProperty("lclsSystm2")
    private String lclsSystm2;
    @JsonProperty("lclsSystm3")
    private String lclsSystm3;
    private String overview;
}
