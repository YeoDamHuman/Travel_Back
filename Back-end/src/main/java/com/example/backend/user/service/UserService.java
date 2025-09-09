package com.example.backend.user.service;

import com.example.backend.board.entity.Board;
import com.example.backend.board.repository.BoardRepository;
import com.example.backend.cart.entity.Cart;
import com.example.backend.cart.repository.CartRepository;
import com.example.backend.comment.entity.Comment;
import com.example.backend.comment.repository.CommentRepository;
import com.example.backend.favorite.entity.Favorite;
import com.example.backend.favorite.repository.FavoriteRepository;
import com.example.backend.jwt.config.JWTGenerator;
import com.example.backend.jwt.dto.JwtDto;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.repository.ScheduleRepository;
import com.example.backend.tour.repository.TourRepository;
import com.example.backend.user.dto.request.UserRequest;
import com.example.backend.user.dto.request.UserRequest.passwordResetRequest;
import com.example.backend.user.dto.response.UserResponse;
import com.example.backend.user.entity.User;
import com.example.backend.user.filter.UserFilter;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.common.auth.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTGenerator jwtGenerator;
    private final CartRepository cartRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final ScheduleRepository scheduleRepository;
    private final TourRepository tourRepository;
    private final UserFilter userFilter;

    // 1️⃣ 회원가입 로직
    @Transactional
    public void register(UserRequest.registerRequest request) {
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        String defaultProfileImageUrl = "https://res.cloudinary.com/dwgnkkmau/image/upload/v1751262353/profile-images/cmjlq1hbb7dqklydlfvd.png";
        String profileImageToSave = (request.getUserProfileImage() != null && !request.getUserProfileImage().isEmpty())
                ? request.getUserProfileImage()
                : defaultProfileImageUrl;

        User user = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .userNickname(request.getUserNickname())
                .userName(request.getUserName())
                .userRole(User.Role.USER)
                .userProfileImage(profileImageToSave)
                .build();

        User savedUser = userRepository.save(user);
    }

    // 2️⃣ 유저정보 변경 로직
    @Transactional
    public UserResponse.updateResponse update(UserRequest.updateRequest request) {
        User user = userRepository.findById(AuthUtil.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (request.getEmail() != null) {
            if (!isValidEmail(request.getEmail())) {
                throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
            }
            if (userRepository.existsByEmailAndUserIdNot(request.getEmail(), user.getUserId())) {
                throw new IllegalArgumentException("이미 등록된 이메일입니다.");
            }
        }

        user.updateUserInfo(
                request.getEmail(),
                request.getUserName(),
                request.getUserNickname(),
                request.getUserProfileImage()
        );

        return UserResponse.updateResponse.builder()
                .email(user.getEmail())
                .userName(user.getUserName())
                .userNickname(user.getUserNickname())
                .userProfileImage(user.getUserProfileImage())
                .build();
    }

    // 3️⃣ 유저 탈퇴 로직
    @Transactional
    public UserResponse.deleteResponse delete() {
        User user = userRepository.findById(AuthUtil.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        log.info("회원 탈퇴 시작 - 사용자 ID: {}", user.getUserId());

        // 1. 댓글 삭제
        List<Comment> userComments = commentRepository.findByUserId(user);
        if (!userComments.isEmpty()) {
            commentRepository.deleteAll(userComments);
            log.info("사용자 댓글 삭제 완료 - 개수: {}", userComments.size());
        }

        // 2. 게시글 삭제 (BoardImage는 cascade로 자동 삭제)
        List<Board> userBoards = boardRepository.findByUserId(user);
        if (!userBoards.isEmpty()) {
            boardRepository.deleteAll(userBoards);
            log.info("사용자 게시글 삭제 완료 - 개수: {}", userBoards.size());
        }

        // 3. 카트 삭제 (먼저 투어 삭제 후 카트 삭제)
        List<Cart> userCarts = cartRepository.findAllByUserId(user);
        if (!userCarts.isEmpty()) {
            for (Cart cart : userCarts) {
                // 먼저 각 카트의 투어들을 삭제
                tourRepository.deleteAllByCartId(cart);
            }
            // 그 다음 카트들을 삭제
            cartRepository.deleteAll(userCarts);
            log.info("사용자 카트 삭제 완료 - 개수: {}", userCarts.size());
        }

        // 4. 즐겨찾기 삭제
        List<Favorite> userFavorites = favoriteRepository.findByUserOrderByCreatedAtDesc(user);
        if (!userFavorites.isEmpty()) {
            favoriteRepository.deleteAll(userFavorites);
            log.info("사용자 즐겨찾기 삭제 완료 - 개수: {}", userFavorites.size());
        }

        // 5. 그룹 스케줄에서 사용자 제거 (스케줄 자체는 유지)
        List<Schedule> userSchedules = scheduleRepository.findAllByUsersContaining(user);
        for (Schedule schedule : userSchedules) {
            schedule.getUsers().remove(user);
            scheduleRepository.save(schedule);
        }
        if (!userSchedules.isEmpty()) {
            log.info("그룹 스케줄에서 사용자 제거 완료 - 개수: {}", userSchedules.size());
        }

        // 6. 최종 사용자 삭제
        userRepository.delete(user);
        log.info("회원 탈퇴 완료 - 사용자 ID: {}", user.getUserId());
        
        return UserResponse.deleteResponse.builder()
                .message("회원 탈퇴가 완료되었습니다.")
                .build();
    }

    // 4️⃣ 유저 정보 조회
    @Transactional
    public UserResponse.InformationResponse myInfo() {
        User user = userRepository.findById(AuthUtil.getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return UserResponse.InformationResponse.builder()
                .email(user.getEmail())
                .userName(user.getUserName())
                .userNickname(user.getUserNickname())
                .userProfileImage(user.getUserProfileImage())
                .build();
    }

    // 5️⃣ 로그인 로직
    @Transactional
    public UserResponse.loginResponse login(UserRequest.loginRequest request) {
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

        userFilter.resetLoginFailures(email);

        log.info("로그인 성공 - 사용자 이메일 : {}", email);

        JwtDto jwtDto = jwtGenerator.generateToken(user);

        return UserResponse.loginResponse.builder()
                .jwtDto(jwtDto)
                .userNickname(user.getUserNickname())
                .userProfileImage(user.getUserProfileImage())
                .userRole(user.getUserRole())
                .build();
    }

    // 6️⃣ 비밀번호 변경 로직
    @Transactional
    public void passwordReset(passwordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("등록된 사용자가 없습니다."));

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    // ✅ 이메일 유효성 체크
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }
}
