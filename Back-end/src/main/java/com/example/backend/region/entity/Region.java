package com.example.backend.region.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Table(name = "region", indexes = {
        @Index(name = "idx_region_code", columnList = "region_code"),
        @Index(name = "idx_ldong_codes", columnList = "l_dong_regn_cd, l_dong_signgu_cd"),
        @Index(name = "idx_view_count", columnList = "view_count DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Region {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Long regionId;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Column(name = "region_code", nullable = false, length = 10)
    private String regionCode;

    @Column(name = "l_dong_regn_cd", length = 10)
    private String lDongRegnCd;

    @Column(name = "l_dong_signgu_cd", length = 10)
    private String lDongSignguCd;

    @Column(name = "region_image", length = 500)
    private String regionImage;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "view_count", nullable = false)
    @ColumnDefault("0")
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    public synchronized void incrementViewCount() {
        this.viewCount++;
        this.lastViewedAt = LocalDateTime.now();
    }
}