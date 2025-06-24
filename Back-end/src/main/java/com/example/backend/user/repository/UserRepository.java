package com.example.backend.user.repository;

import com.example.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 이메일 중복 체크 시 자기 자신 제외하고 확인하는 메서드
    boolean existsByEmailAndUserIdNot(String email, UUID userId);

}
