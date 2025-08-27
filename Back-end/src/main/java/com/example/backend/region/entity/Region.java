package com.example.backend.region.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "region")
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

    @Column(name = "region_image", length = 500)
    private String regionImage;

    @Column(name = "description", length = 200)
    private String description;
}