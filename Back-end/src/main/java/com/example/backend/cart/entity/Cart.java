package com.example.backend.cart.entity;

import com.example.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

    @Column(name = "region", length = 255)
    private String region;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "budget")
    private BigDecimal budget;

    @Column(name = "total_expense")
    private BigDecimal totalExpense;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    public void updateRegion(String region) {
        this.region = region;
    }

    public void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
    }
}