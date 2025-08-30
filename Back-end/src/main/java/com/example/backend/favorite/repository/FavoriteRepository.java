package com.example.backend.favorite.repository;

import com.example.backend.favorite.entity.Favorite;
import com.example.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {
    
    List<Favorite> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<Favorite> findByUserAndContentId(User user, String contentId);
    
    boolean existsByUserAndContentId(User user, String contentId);
    
    void deleteByUserAndContentId(User user, String contentId);
}