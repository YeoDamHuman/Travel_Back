package com.example.backend.tour.repository;

import com.example.backend.cart.entity.Cart;
import com.example.backend.tour.entity.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TourRepository extends JpaRepository<Tour, UUID> {
    List<Tour> findByCartId(Cart cart);

    boolean existsByCartIdAndAddress(Cart cart, String address);

    boolean existsByCartIdAndContentId(Cart cart, String contentId);

    void deleteByCartIdAndTourId(Cart cart, UUID tourId);

    void deleteAllByCartId(Cart cart);

    int countByCartId(Cart cart);
}