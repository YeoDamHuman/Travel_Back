package com.example.backend.tour.entity;

import com.example.backend.cart.entity.Cart;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tour")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "tour_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID tourId;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "address", length = 500)
    private String address;

    @Column(name = "address2", length = 500)
    private String address2;

    @Column(name = "zipcode", length = 20)
    private String zipcode;

    @Column(name = "area_code", length = 100)
    private String areaCode;

    @Column(name = "cat1", length = 100)
    private String cat1;

    @Column(name = "cat2", length = 100)
    private String cat2;

    @Column(name = "cat3", length = 100)
    private String cat3;

    @Column(name = "created_time", length = 20)
    private String createdTime;

    @Column(name = "first_image", length = 500)
    private String firstImage;

    @Column(name = "first_image2", length = 500)
    private String firstImage2;

    @Column(name = "cpyrht_div_cd", length = 50)
    private String cpyrhtDivCd;

    @Column(name = "mapx", length = 100)
    private String mapX;

    @Column(name = "mapy", length = 100)
    private String mapY;

    @Column(name = "mlevel", length = 10)
    private String mlevel;

    @Column(name = "modified_time", length = 20)
    private String modifiedTime;

    @Column(name = "sigungu_code", length = 100)
    private String sigunguCode;

    @Column(name = "tel", length = 100)
    private String tel;

    @Column(name = "overview", length = 2000)
    private String overview;

    @Column(name = "l_dong_regn_cd", length = 50)
    private String lDongRegnCd;

    @Column(name = "l_dong_signgu_cd", length = 50)
    private String lDongSignguCd;

    @Column(name = "lcls_systm1", length = 50)
    private String lclsSystm1;

    @Column(name = "lcls_systm2", length = 50)
    private String lclsSystm2;

    @Column(name = "lcls_systm3", length = 50)
    private String lclsSystm3;

    @Column(name = "content_id", length = 100, unique = true)
    private String contentId;

    @Column(name = "content_type_id", length = 100)
    private String contentTypeId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "image", length = 500)
    private String image;

    @Column(name = "tema", length = 100)
    private String tema;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private TourCategory category;

    @Column(name = "price")
    private Long price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cartId;
}
