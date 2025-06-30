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