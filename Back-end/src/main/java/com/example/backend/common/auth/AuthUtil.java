package com.example.backend.common.auth;

import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public class AuthUtil {

    // ✅ 현재 사용자 ID 가져오기
    public static UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다.");
        }
        return UUID.fromString(authentication.getName());
    }

    public static User getCurrentUser(UserRepository userRepository) {
        UUID userId = getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자를 찾을 수 없습니다."));
    }
}