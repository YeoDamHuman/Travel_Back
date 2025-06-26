package com.example.backend.cart.service;

import com.example.backend.cart.dto.request.CartRequest;
import com.example.backend.cart.dto.response.CartResponse;
import com.example.backend.cart.entity.Cart;
import com.example.backend.cart.repository.CartRepository;
import com.example.backend.tour.entity.Tour;
import com.example.backend.tour.repository.TourRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.tour.webclient.TourApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourApiClient tourApiClient;

    @Transactional  // ← readOnly = true 제거!
    public CartResponse.CartDetailResponse getCart(String userIdString) {
        // UUID 문자열을 UUID 객체로 변환
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID 형식입니다.");
        }

        // UUID로 사용자 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUserId(user)
                .orElseGet(() -> createNewCart(user));

        List<Tour> tours = tourRepository.findByCartId(cart);

        return CartResponse.CartDetailResponse.builder()
                .cartId(cart.getCartId())
                .region(cart.getRegion())
                .tours(tours.stream()
                        .map(tour -> CartResponse.TourDetailResponse.builder()
                                .tourId(tour.getTourId())
                                .longitude(tour.getLongitude())
                                .latitude(tour.getLatitude())
                                .address(tour.getAddress())
                                .image(tour.getImage())
                                .tema(tour.getTema())
                                .build())
                        .collect(Collectors.toList()))
                .totalCount(tours.size())
                .build();
    }

    @Transactional
    public CartResponse.AddTourResponse addTourToCart(String userIdString, CartRequest.AddTourRequest request) {
        // UUID 문자열을 UUID 객체로 변환
        UUID userId;
        try {
            userId = UUID.fromString(userIdString);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID 형식입니다.");
        }

        // UUID로 사용자 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUserId(user)
                .orElseGet(() -> createNewCart(user));

        // 이미 같은 주소의 투어가 장바구니에 있는지 확인
        boolean isDuplicate = tourRepository.existsByCartIdAndAddress(cart, request.getAddress());
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 장바구니에 추가된 투어입니다.");
        }

        Tour tour = Tour.builder()
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .address(request.getAddress())
                .image(request.getImage())
                .tema(request.getTema())
                .cartId(cart)
                .build();

        Tour savedTour = tourRepository.save(tour);

        // 지역 정보 업데이트 (첫 번째 투어 추가 시)
        if (cart.getRegion() == null || cart.getRegion().isEmpty()) {
            String region = extractRegionFromAddress(request.getAddress());
            cart = Cart.builder()
                    .cartId(cart.getCartId())
                    .region(region)
                    .userId(cart.getUserId())
                    .build();
            cartRepository.save(cart);
        }

        return CartResponse.AddTourResponse.builder()
                .tourId(savedTour.getTourId())
                .message("투어가 장바구니에 추가되었습니다.")
                .build();
    }

    @Transactional(readOnly = true)  // ← 검색은 readOnly 유지
    public Page<CartResponse.TourSearchResponse> searchTours(String keyword, String region, String category, Pageable pageable) {
        // 한국관광공사 TourAPI 호출
        return tourApiClient.searchTours(keyword, region, category, pageable);
    }

    private Cart createNewCart(User user) {
        Cart cart = Cart.builder()
                .userId(user)  // User 객체를 userId 필드에 저장
                .region("서울")
                .build();
        return cartRepository.save(cart);
    }

    private String extractRegionFromAddress(String address) {
        // 주소에서 시/도 정보 추출
        String[] parts = address.split(" ");
        if (parts.length > 0) {
            return parts[0]; // 첫 번째 부분이 보통 시/도
        }
        return "기타";
    }
}