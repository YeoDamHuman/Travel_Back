package com.example.backend.user.service;

import com.example.backend.jwt.config.JWTGenerator;
import com.example.backend.jwt.dto.JwtDto;
import com.example.backend.user.dto.request.UserRequest;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;  // JWTGenerator 주입

    public void register(UserRequest.registerRequest register) {
        // 이메일 형식 체크
        if (!isValidEmail(register.getEmail())) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        } else if (userRepository.existsByEmail(register.getEmail())) {
            throw new IllegalArgumentException("이미 등록된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(register.getPassword());

        User user = User.builder()
                .email(register.getEmail())
                .password(encodedPassword)
                .address(register.getAddress())
                .userName(register.getUserName())
                .userRole(User.Role.USER)
                .build();
        userRepository.save(user);
    }


    public JwtDto login(UserRequest.loginRequest login) {
        User user = userRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일이 존재하지 않습니다."));

        if(!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        log.info("로그인 성공 - 사용자 이메일 : {}", login.getEmail());
        return jwtGenerator.generateToken(user);  // 로그인 성공 시 토큰 생성 및 반환
    }

    // 이메일 유효성 검사 메서드
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }
}
