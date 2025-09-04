package com.example.backend.favorite.entity;

import com.example.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "favorite")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "favorite_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID favoriteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    private User user;

    @Column(name = "content_id", nullable = false, length = 100)
    private String contentId;

    @Column(name = "place_title", length = 200)
    private String placeTitle;

    @Column(name = "place_address", length = 500)
    private String placeAddress;

    @Column(name = "place_image", length = 500)
    private String placeImage;

    @Column(name = "region_code", length = 10)
    private String regionCode;

    @Column(name = "l_dong_regn_cd", length = 10)
    private String lDongRegnCd;

    @Column(name = "l_dong_signgu_cd", length = 10)
    private String lDongSignguCd;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}