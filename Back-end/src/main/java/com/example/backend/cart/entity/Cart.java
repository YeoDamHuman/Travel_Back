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

    @Column(name = "region", length = 255, nullable = false)
    private String region;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "budget")
    private BigDecimal budget;

    @Column(name = "total_expense")
    private BigDecimal totalExpense;
}
