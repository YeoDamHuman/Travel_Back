package com.example.backend.cart.entity;

import com.example.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "cart_id", columnDefinition = "BINARY(16)", nullable = false)
    private UUID cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "BINARY(16)", nullable = false)
    private User user;

    @Column(name = "region", length = 255, nullable = false)
    private String region;

    @Column(name = "budget", nullable = false, columnDefinition = "BIGINT")
    private Long budget;

    public void updateRegion(String region) {
        this.region = region;
    }

    public void updateBudget(Long budget) {
        this.budget = budget;
    }
}