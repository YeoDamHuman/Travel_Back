package com.example.backend.cart.repository;

import com.example.backend.cart.entity.Cart;
import com.example.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUserId(User user);

    boolean existsByUserId(User user);

    List<Cart> findAllByUserId(User user);

    Optional<Cart> findByCartIdAndUserId(UUID cartId, User user);
}