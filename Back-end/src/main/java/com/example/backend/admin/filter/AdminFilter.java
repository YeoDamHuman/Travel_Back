package com.example.backend.admin.filter;

import com.example.backend.user.dto.request.UserRequest;
import com.example.backend.user.entity.User;
import com.example.backend.user.filter.UserFilter;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminFilter {

    private final UserRepository userRepository;
    private final UserFilter userFilter;
    private final PasswordEncoder passwordEncoder;

    public void isAdmin(UserRequest.loginRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    userFilter.recordLoginFailure(email);
                    return new IllegalArgumentException("이메일이 존재하지 않습니다.");
                });

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            userFilter.recordLoginFailure(email);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if(user.getUserRole() == User.Role.ADMIN) {
            
        }else {

        }
    }
}
