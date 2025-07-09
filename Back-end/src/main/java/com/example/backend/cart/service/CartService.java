package com.example.backend.cart.service;

import com.example.backend.cart.dto.response.CartResponse;
import com.example.backend.cart.entity.Cart;
import com.example.backend.cart.entity.CartItem;
import com.example.backend.cart.repository.CartItemRepository;
import com.example.backend.cart.repository.CartRepository;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.tour.entity.Tour;
import com.example.backend.tour.service.TourService;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final TourService tourService;

    @Transactional(readOnly = true)
    public CartResponse.CartDetailResponse getCart(String userIdString) {
        User user = findUserById(userIdString);
        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null) {
            return CartResponse.CartDetailResponse.empty();
        }

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        List<CartResponse.TourInfo> tourInfos = cartItems.stream()
                .map(cartItem -> mapToTourInfo(cartItem.getTour()))
                .collect(Collectors.toList());

        long totalPrice = tourInfos.stream()
                .mapToLong(t -> t.getPrice() != null ? t.getPrice() : 0)
                .sum();

        return CartResponse.CartDetailResponse.builder()
                .cartId(cart.getCartId())
                .region(cart.getRegion())
                .tours(tourInfos)
                .totalCount(tourInfos.size())
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional
    public CartResponse.AddTourResponse addTourToCart(String userIdString, String contentId) {
        User user = findUserById(userIdString);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> createNewCart(user));

        Tour tour = tourService.getOrCreateTour(contentId);

        cartItemRepository.findByCartAndTour(cart, tour).ifPresent(item -> {
            throw new BusinessException("CART_DUPLICATE_TOUR", "이미 장바구니에 추가된 투어입니다.");
        });

        CartItem cartItem = CartItem.createCartItem(cart, tour);
        cartItemRepository.save(cartItem);

        if (cart.getCartItems().isEmpty()) {
            String region = extractRegionFromAddress(tour.getAddress());
            cart.updateRegion(region);
            cartRepository.save(cart);
        }

        return new CartResponse.AddTourResponse(tour.getTourId(), "투어가 장바구니에 추가되었습니다.");
    }

    @Transactional
    public void removeTourFromCart(String userIdString, UUID tourId) {
        User user = findUserById(userIdString);
        Cart cart = findCartByUser(user);
        cartItemRepository.deleteByCartAndTour_TourId(cart, tourId);
        log.info("투어 삭제 완료 - tourId: {}", tourId);
    }

    @Transactional
    public void clearCart(String userIdString) {
        User user = findUserById(userIdString);
        Cart cart = findCartByUser(user);
        cartItemRepository.deleteAllByCart(cart);
        log.info("장바구니 전체 삭제 완료 - userId: {}", userIdString);
    }

    private User findUserById(String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    private Cart findCartByUser(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));
    }

    private Cart createNewCart(User user) {
        Cart cart = Cart.builder().user(user).build();
        return cartRepository.save(cart);
    }

    private String extractRegionFromAddress(String address) {
        if (address == null || address.isEmpty()) return "기타";
        String[] parts = address.split(" ");
        if (parts.length > 1) {
            if (parts[0].matches("^(서울|부산|대구|인천|광주|대전|울산)광역시$")) {
                return parts[0];
            } else if (parts[0].matches("^(경기도|강원도|충청북도|충청남도|전라북도|전라남도|경상북도|경상남도|제주특별자치도)$")) {
                return parts[0];
            }
        }
        return parts.length > 0 ? parts[0] : "기타";
    }

    private CartResponse.TourInfo mapToTourInfo(Tour tour) {
        return CartResponse.TourInfo.builder()
                .tourId(tour.getTourId())
                .longitude(tour.getLongitude())
                .latitude(tour.getLatitude())
                .address(tour.getAddress())
                .image(tour.getImage())
                .tema(tour.getTema())
                .category(tour.getCategory() != null ? tour.getCategory().getDescription() : null)
                .price(tour.getPrice())
                .build();
    }
}