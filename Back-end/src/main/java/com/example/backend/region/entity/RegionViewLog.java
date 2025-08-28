package com.example.backend.region.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "region_view_log", indexes = {
        @Index(name = "idx_ip_region_created", columnList = "ip_address, region_code, created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RegionViewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "ip_address", nullable = false, length = 45) // IPv6 지원
    private String ipAddress;

    @Column(name = "region_code", nullable = false, length = 10)
    private String regionCode;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}