package com.example.backend.cart.service;

import com.example.backend.cart.dto.request.CartRequest;
import com.example.backend.cart.dto.response.CartResponse;
import com.example.backend.cart.entity.Cart;
import com.example.backend.cart.repository.CartRepository;
import com.example.backend.tour.dto.response.TourDetailResponse;
import com.example.backend.tour.entity.Tour;
import com.example.backend.tour.entity.TourCategory;
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

    @Transactional
    public CartResponse.CartDetailResponse getCart(String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUserId(user)
                .orElse(null);

        if (cart == null) {
            return CartResponse.CartDetailResponse.builder()
                    .cartId(null)
                    .lDongRegnCd("11")
                    .lDongSignguCd("110")
                    .tours(List.of())
                    .totalCount(0)
                    .totalPrice(0L)
                    .build();
        }

        List<Tour> tours = tourRepository.findByCartId(cart);

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
                .lDongRegnCd(cart.getLDongRegnCd())
                .lDongSignguCd(cart.getLDongSignguCd())
                .tours(tourInfos)
                .totalCount(tourInfos.size())
                .totalPrice(totalPrice)
                .build();
    } 

    @Transactional
    public CartResponse.AddTourResponse addTourToCart(String userIdString, CartResponse.TourSearchResponse tourResponse) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUserId(user)
                .orElseGet(() -> createNewCart(user));

        boolean isDuplicate = tourRepository.existsByCartIdAndAddress(cart, tourResponse.getAddress());
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 장바구니에 추가된 투어입니다.");
        }

        Tour tour = Tour.builder()
                .contentId(tourResponse.getContentId())
                .contentTypeId(tourResponse.getContentTypeId())
                .title(tourResponse.getTitle())
                .address(tourResponse.getAddress())
                .address2(tourResponse.getAddress2())
                .zipcode(tourResponse.getZipcode())
                .areaCode(tourResponse.getAreaCode())
                .cat1(tourResponse.getCat1())
                .cat2(tourResponse.getCat2())
                .cat3(tourResponse.getCat3())
                .createdTime(tourResponse.getCreatedTime())
                .firstImage(tourResponse.getFirstImage())
                .firstImage2(tourResponse.getFirstImage2())
                .cpyrhtDivCd(tourResponse.getCpyrhtDivCd())
                .mapX(tourResponse.getMapX())
                .mapY(tourResponse.getMapY())
                .mlevel(tourResponse.getMlevel())
                .modifiedTime(tourResponse.getModifiedTime())
                .sigunguCode(tourResponse.getSigunguCode())
                .tel(tourResponse.getTel())
                .overview("") // 현재 TourSearchResponse에는 overview가 없으므로 기본값
                .lDongRegnCd(tourResponse.getLDongRegnCd())
                .lDongSignguCd(tourResponse.getLDongSignguCd())
                .lclsSystm1(tourResponse.getLclsSystm1())
                .lclsSystm2(tourResponse.getLclsSystm2())
                .lclsSystm3(tourResponse.getLclsSystm3())
                .longitude(tourResponse.getMapX() != null && !tourResponse.getMapX().isEmpty()
                        ? Double.valueOf(tourResponse.getMapX()) : null)
                .latitude(tourResponse.getMapY() != null && !tourResponse.getMapY().isEmpty()
                        ? Double.valueOf(tourResponse.getMapY()) : null)
                .image(tourResponse.getFirstImage())
                .tema("") // 필요에 따라 설정
                .cartId(cart)
                .build();

        Tour savedTour = tourRepository.save(tour);

        if (cart.getLDongRegnCd() == null || cart.getLDongRegnCd().isEmpty()) {
            cart = Cart.builder()
                    .cartId(cart.getCartId())
                    .lDongRegnCd("11")
                    .lDongSignguCd("110")
                    .userId(cart.getUserId())
                    .build();
            cartRepository.save(cart);
        }

        return CartResponse.AddTourResponse.builder()
                .tourId(savedTour.getTourId())
                .message("투어가 장바구니에 추가되었습니다.")
                .build();
    }

    @Transactional
    public void removeTourFromCart(String userIdString, UUID tourId) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUserId(user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        tourRepository.deleteByCartIdAndTourId(cart, tourId);
        log.info("투어 삭제 완료 - tourId: {}", tourId);
    }

    @Transactional
    public void clearCart(String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByUserId(user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        tourRepository.deleteAllByCartId(cart);
        log.info("장바구니 전체 삭제 완료 - userId: {}", userId);
    }

    @Transactional(readOnly = true)
    public Page<CartResponse.TourSearchResponse> searchTours(String keyword, String region, String category, Pageable pageable) {
        return tourApiClient.searchTours(keyword, region, category, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CartResponse.TourSearchResponse> searchPlacesByRegion(String regionCode, Pageable pageable) {
        return tourApiClient.searchPlacesByRegion(regionCode, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CartResponse.TourSearchResponse> searchPlacesByRegionAndTheme(String regionCode, String theme, Pageable pageable) {
        return tourApiClient.searchPlacesByRegionAndTheme(regionCode, theme, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CartResponse.TourSearchResponse> searchPlacesByLDong(String lDongRegnCd, String lDongSignguCd, Pageable pageable) {
        return tourApiClient.searchPlacesByLDong(lDongRegnCd, lDongSignguCd, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CartResponse.TourSearchResponse> searchPlacesByLDongAndContentType(String lDongRegnCd, String lDongSignguCd, Integer contentTypeId, Pageable pageable) {
        return tourApiClient.searchPlacesByLDongAndContentType(lDongRegnCd, lDongSignguCd, contentTypeId, pageable);
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

        if (cart.getLDongRegnCd() == null || cart.getLDongRegnCd().isEmpty()) {
            cart = Cart.builder()
                    .cartId(cart.getCartId())
                    .lDongRegnCd("11")
                    .lDongSignguCd("110")
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
        Cart cart = Cart.builder()
                .userId(user)
                .lDongRegnCd("11")
                .lDongSignguCd("110")
                .build();
        return cartRepository.save(cart);
    }

    private String extractRegionFromAddress(String address) {
        String[] parts = address.split(" ");
        if (parts.length > 0) {
            return parts[0];
        }
        return "기타";
    }

    @Transactional
    public CartResponse.CartDetailResponse createCart(String userIdString, String lDongRegnCd, String lDongSignguCd) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = Cart.builder()
                .userId(user)
                .lDongRegnCd(lDongRegnCd != null ? lDongRegnCd : "11")
                .lDongSignguCd(lDongSignguCd != null ? lDongSignguCd : "110")
                .build();
        Cart savedCart = cartRepository.save(cart);

        return CartResponse.CartDetailResponse.builder()
                .cartId(savedCart.getCartId())
                .lDongRegnCd(savedCart.getLDongRegnCd())
                .lDongSignguCd(savedCart.getLDongSignguCd())
                .tours(List.of())
                .totalCount(0)
                .totalPrice(0L)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CartResponse.CartDetailResponse> getUserCarts(String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<Cart> carts = cartRepository.findAllByUserId(user);

        return carts.stream()
                .map(cart -> {
                    List<Tour> tours = tourRepository.findByCartId(cart);
                    
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
                            .lDongRegnCd(cart.getLDongRegnCd())
                .lDongSignguCd(cart.getLDongSignguCd())
                            .tours(tourInfos)
                            .totalCount(tourInfos.size())
                            .totalPrice(totalPrice)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public CartResponse.CartDetailResponse getCartById(String userIdString, UUID cartId) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByCartIdAndUserId(cartId, user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        List<Tour> tours = tourRepository.findByCartId(cart);

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
                .lDongRegnCd(cart.getLDongRegnCd())
                .lDongSignguCd(cart.getLDongSignguCd())
                .tours(tourInfos)
                .totalCount(tourInfos.size())
                .totalPrice(totalPrice)
                .build();
    }

    @Transactional
    public void deleteCart(String userIdString, UUID cartId) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByCartIdAndUserId(cartId, user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        tourRepository.deleteAllByCartId(cart);
        cartRepository.delete(cart);
        log.info("장바구니 삭제 완료 - cartId: {}", cartId);
    }

    @Transactional
    public CartResponse.AddTourResponse addTourToSpecificCart(String userIdString, UUID cartId, CartResponse.TourSearchResponse tourResponse) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByCartIdAndUserId(cartId, user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        boolean isDuplicate = tourRepository.existsByCartIdAndAddress(cart, tourResponse.getAddress());
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 장바구니에 추가된 투어입니다.");
        }

        Tour tour = Tour.builder()
                .contentId(tourResponse.getContentId())
                .contentTypeId(tourResponse.getContentTypeId())
                .title(tourResponse.getTitle())
                .address(tourResponse.getAddress())
                .address2(tourResponse.getAddress2())
                .zipcode(tourResponse.getZipcode())
                .areaCode(tourResponse.getAreaCode())
                .cat1(tourResponse.getCat1())
                .cat2(tourResponse.getCat2())
                .cat3(tourResponse.getCat3())
                .createdTime(tourResponse.getCreatedTime())
                .firstImage(tourResponse.getFirstImage())
                .firstImage2(tourResponse.getFirstImage2())
                .cpyrhtDivCd(tourResponse.getCpyrhtDivCd())
                .mapX(tourResponse.getMapX())
                .mapY(tourResponse.getMapY())
                .mlevel(tourResponse.getMlevel())
                .modifiedTime(tourResponse.getModifiedTime())
                .sigunguCode(tourResponse.getSigunguCode())
                .tel(tourResponse.getTel())
                .overview("")
                .lDongRegnCd(tourResponse.getLDongRegnCd())
                .lDongSignguCd(tourResponse.getLDongSignguCd())
                .lclsSystm1(tourResponse.getLclsSystm1())
                .lclsSystm2(tourResponse.getLclsSystm2())
                .lclsSystm3(tourResponse.getLclsSystm3())
                .longitude(tourResponse.getMapX() != null && !tourResponse.getMapX().isEmpty()
                        ? Double.valueOf(tourResponse.getMapX()) : null)
                .latitude(tourResponse.getMapY() != null && !tourResponse.getMapY().isEmpty()
                        ? Double.valueOf(tourResponse.getMapY()) : null)
                .image(tourResponse.getFirstImage())
                .tema("")
                .cartId(cart)
                .build();

        Tour savedTour = tourRepository.save(tour);

        return CartResponse.AddTourResponse.builder()
                .tourId(savedTour.getTourId())
                .message("투어가 장바구니에 추가되었습니다.")
                .build();
    }

    @Transactional
    public CartResponse.AddTourResponse addTourToSpecificCartByContentId(String userIdString, UUID cartId, String contentId) {
        UUID userId = UUID.fromString(userIdString);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Cart cart = cartRepository.findByCartIdAndUserId(cartId, user)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다."));

        boolean isDuplicate = tourRepository.existsByCartIdAndContentId(cart, contentId);
        if (isDuplicate) {
            throw new IllegalArgumentException("이미 장바구니에 추가된 투어입니다.");
        }

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

        return CartResponse.AddTourResponse.builder()
                .tourId(savedTour.getTourId())
                .message("투어가 장바구니에 추가되었습니다.")
                .build();
    }

    @Transactional(readOnly = true)
    public Cart findCartById(UUID cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 장바구니입니다. cartId: " + cartId));
    }
}
