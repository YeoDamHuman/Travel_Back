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
    private String homepage;
    private String tel;
    private String telname;
    private String firstimage;
    private String firstimage2;
    private String addr1;
    private String addr2;
    private String zipcode;
    private String mapx;
    private String mapy;
    private String mlevel;
    private String overview;
}