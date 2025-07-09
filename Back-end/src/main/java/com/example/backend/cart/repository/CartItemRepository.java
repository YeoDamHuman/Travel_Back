package com.example.backend.cart.repository;

import com.example.backend.cart.entity.Cart;
import com.example.backend.cart.entity.CartItem;
import com.example.backend.tour.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    Optional<CartItem> findByCartAndTour(Cart cart, Tour tour);

    List<CartItem> findByCart(Cart cart);

    void deleteByCartAndTour_TourId(Cart cart, UUID tourId);

    void deleteAllByCart(Cart cart);
}
