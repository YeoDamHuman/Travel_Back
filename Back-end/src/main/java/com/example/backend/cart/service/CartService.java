package com.example.backend.cart.service;

import com.example.backend.cart.dto.request.AddTourRequest;
import com.example.backend.cart.dto.response.CartResponse;
import com.example.backend.cart.entity.Cart;
import com.example.backend.cart.repository.CartRepository;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.tour.entity.Tour;
import com.example.backend.tour.repository.TourRepository;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse.CartDetailResponse getCart(String userIdString) {
        User user = findUserById(userIdString);
        Cart cart = cartRepository.findByUser(user).orElse(null);

        if (cart == null) {
            return CartResponse.CartDetailResponse.empty();
        }

        List<Tour> tours = tourRepository.findByCart(cart);
        List<CartResponse.TourInfo> tourInfos = tours.stream()
                .map(tour -> CartResponse.TourInfo.builder()
                        .tourId(tour.getTourId())
                        .contentId(tour.getContentId())
                        .title(tour.getTitle())
                        .image(tour.getImage())
                        .tema(tour.getTema())
                        .longitude(tour.getLongitude())
                        .latitude(tour.getLatitude())
                        .address(tour.getAddress())
                        .category(tour.getCategory())
                        .price(tour.getPrice())
                        .build())
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
    public CartResponse.AddTourResponse addTourToCart(String userIdString, AddTourRequest request) {
        User user = findUserById(userIdString);
        Cart cart = cartRepository.findByUser(user).orElseGet(() -> createNewCart(user));

        Tour existingTour = tourRepository.findByContentId(request.getContentId()).orElse(null);
        if (existingTour != null && existingTour.getCart() != null) {
            throw new BusinessException("CART_DUPLICATE_TOUR", "이미 장바구니에 추가된 투어입니다.");
        }

        Tour tour = Tour.builder()
                .cart(cart)
                .contentId(request.getContentId())
                .contentTypeId(request.getContentTypeId())
                .title(request.getTitle())
                .address(request.getAddress())
                .address2(request.getAddress2())
                .zipcode(request.getZipcode())
                .areaCode(request.getAreaCode())
                .cat1(request.getCat1())
                .cat2(request.getCat2())
                .cat3(request.getCat3())
                .createdTime(request.getCreatedTime())
                .firstImage(request.getFirstImage())
                .firstImage2(request.getFirstImage2())
                .cpyrhtDivCd(request.getCpyrhtDivCd())
                .mapX(request.getMapX())
                .mapY(request.getMapY())
                .mlevel(request.getMlevel())
                .modifiedTime(request.getModifiedTime())
                .sigunguCode(request.getSigunguCode())
                .tel(request.getTel())
                .overview(request.getOverview())
                .lDongRegnCd(request.getLdongRegnCd())
                .lDongSignguCd(request.getLdongSignguCd())
                .lclsSystm1(request.getLclsSystm1())
                .lclsSystm2(request.getLclsSystm2())
                .lclsSystm3(request.getLclsSystm3())
                .longitude(request.getMapX() != null && !request.getMapX().isEmpty() ? new java.math.BigDecimal(request.getMapX()) : null)
                .latitude(request.getMapY() != null && !request.getMapY().isEmpty() ? new java.math.BigDecimal(request.getMapY()) : null)
                .image(request.getFirstImage())
                .build();
        
        tourRepository.save(tour);

        if (cart.getRegion() == null || cart.getRegion().isEmpty()) {
            String region = extractRegionFromAddress(request.getAddress());
            cart.updateRegion(region);
            cartRepository.save(cart);
        }

        return new CartResponse.AddTourResponse(tour.getTourId(), "투어가 장바구니에 추가되었습니다.");
    }

    @Transactional
    public void removeTourFromCart(String userIdString, UUID tourId) {
        User user = findUserById(userIdString);
        Cart cart = findCartByUser(user);
        tourRepository.deleteByCartAndTourId(cart, tourId);
    }

    @Transactional
    public void clearCart(String userIdString) {
        User user = findUserById(userIdString);
        Cart cart = findCartByUser(user);
        tourRepository.deleteAllByCart(cart);
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

    @Transactional(readOnly = true)
    public CartResponse.TourDetailResponse getTourDetail(String contentId, String userIdString) {
        CartResponse.TourDetailResponse detailResponse = tourApiClient.getTourDetail(contentId);
        
        if (userIdString != null) {
            try {
                UUID userId = UUID.fromString(userIdString);
                User user = userRepository.findById(userId).orElse(null);
                
                if (user != null) {
                    Cart cart = cartRepository.findByUserId(user).orElse(null);
                    if (cart != null) {
                        boolean isInCart = tourRepository.existsByCartIdAndContentId(cart, contentId);
                        return CartResponse.TourDetailResponse.builder()
                                .contentId(detailResponse.getContentId())
                                .contentTypeId(detailResponse.getContentTypeId())
                                .title(detailResponse.getTitle())
                                .address(detailResponse.getAddress())
                                .region(detailResponse.getRegion())
                                .theme(detailResponse.getTheme())
                                .latitude(detailResponse.getLatitude())
                                .longitude(detailResponse.getLongitude())
                                .image(detailResponse.getImage())
                                .tel(detailResponse.getTel())
                                .homepage(detailResponse.getHomepage())
                                .overview(detailResponse.getOverview())
                                .isFavorite(false) // TODO: 즐겨찾기 기능 구현 시 수정
                                .isInCart(isInCart)
                                .build();
                    }
                }
            } catch (Exception e) {
                log.warn("사용자 정보 확인 중 오류: {}", e.getMessage());
            }
        }
        
        return CartResponse.TourDetailResponse.builder()
                .contentId(detailResponse.getContentId())
                .contentTypeId(detailResponse.getContentTypeId())
                .title(detailResponse.getTitle())
                .address(detailResponse.getAddress())
                .region(detailResponse.getRegion())
                .theme(detailResponse.getTheme())
                .latitude(detailResponse.getLatitude())
                .longitude(detailResponse.getLongitude())
                .image(detailResponse.getImage())
                .tel(detailResponse.getTel())
                .homepage(detailResponse.getHomepage())
                .overview(detailResponse.getOverview())
                .isFavorite(false)
                .isInCart(false)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<CartResponse.TourSearchResponse> searchToursByTheme(String theme, String region, Pageable pageable) {
        return tourApiClient.searchToursByTheme(theme, region, pageable);
    }

    @Transactional
    public CartResponse.AddTourResponse addTourToCartByContentId(String userIdString, String contentId) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUserId(user)
                .orElseGet(() -> createNewCart(user));

        boolean isDuplicate = tourRepository.existsByCartIdAndContentId(cart, contentId);
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 장바구니에 추가된 투어입니다.");
        }

        // TourAPI에서 상세 정보 가져오기
        CartResponse.TourDetailResponse tourDetail = tourApiClient.getTourDetail(contentId);

        Tour tour = Tour.builder()
                .contentId(contentId)
                .contentTypeId(tourDetail.getContentTypeId())
                .title(tourDetail.getTitle())
                .address(tourDetail.getAddress())
                .longitude(tourDetail.getLongitude())
                .latitude(tourDetail.getLatitude())
                .image(tourDetail.getImage())
                .tel(tourDetail.getTel())
                .overview(tourDetail.getOverview())
                .tema(tourDetail.getTheme())
                .cartId(cart)
                .build();

        Tour savedTour = tourRepository.save(tour);

        if (cart.getRegion() == null || cart.getRegion().isEmpty()) {
            String region = tourDetail.getRegion();
            cart = Cart.builder()
                    .cartId(cart.getCartId())
                    .region(region != null ? region : "기타")
                    .userId(cart.getUserId())
                    .build();
            cartRepository.save(cart);
        }

        return CartResponse.AddTourResponse.builder()
                .tourId(savedTour.getTourId())
                .message("투어가 장바구니에 추가되었습니다.")
                .build();
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
                .address2(tour.getAddress2())
                .zipcode(tour.getZipcode())
                .areaCode(tour.getAreaCode())
                .cat1(tour.getCat1())
                .cat2(tour.getCat2())
                .cat3(tour.getCat3())
                .createdTime(tour.getCreatedTime())
                .firstImage(tour.getFirstImage())
                .firstImage2(tour.getFirstImage2())
                .cpyrhtDivCd(tour.getCpyrhtDivCd())
                .mapX(tour.getMapX())
                .mapY(tour.getMapY())
                .mlevel(tour.getMlevel())
                .modifiedTime(tour.getModifiedTime())
                .sigunguCode(tour.getSigunguCode())
                .tel(tour.getTel())
                .overview(tour.getOverview())
                .lDongRegnCd(tour.getLDongRegnCd())
                .lDongSignguCd(tour.getLDongSignguCd())
                .lclsSystm1(tour.getLclsSystm1())
                .lclsSystm2(tour.getLclsSystm2())
                .lclsSystm3(tour.getLclsSystm3())
                .contentId(tour.getContentId())
                .contentTypeId(tour.getContentTypeId())
                .title(tour.getTitle())
                .image(tour.getImage())
                .tema(tour.getTema())
                .category(tour.getCategory())
                .price(tour.getPrice())
                .thema(tour.getThema())
                .build();
    }
}