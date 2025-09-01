package com.example.backend.cart.controller;

import com.example.backend.cart.dto.request.CartRequest;
import com.example.backend.cart.dto.response.CartResponse;
import com.example.backend.cart.service.CartService;
import com.example.backend.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "CartAPI", description = "장바구니 및 투어 관리 API")
@Slf4j
public class CartController {

    private final CartService cartService;
    private final RegionService regionService;

    @GetMapping("/cart")
    @Operation(summary = "장바구니 조회", description = "사용자의 장바구니 내용 조회",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CartResponse.CartDetailResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        CartResponse.CartDetailResponse response = cartService.getCart(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart/tours")
    @Operation(summary = "장바구니에 투어 추가", description = "장바구니에 새로운 투어 추가",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CartResponse.AddTourResponse> addTourToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CartResponse.TourSearchResponse tourResponse) {
        CartResponse.AddTourResponse response = cartService.addTourToCart(userDetails.getUsername(), tourResponse);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/cart/tours/{tourId}")
    @Operation(summary = "장바구니에서 투어 삭제",
            description = "장바구니에서 특정 투어 삭제",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Void> removeTourFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID tourId) {
        cartService.removeTourFromCart(userDetails.getUsername(), tourId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cart/tours")
    @Operation(summary = "장바구니 전체 비우기",
            description = "장바구니의 모든 투어 삭제",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tour/search")
    @Operation(summary = "투어 검색", description = "공공 API 연동을 통한 투어 검색")
    public ResponseEntity<Page<CartResponse.TourSearchResponse>> searchTours(
            @Parameter(description = "검색 키워드 (예: 해운대)", example = "해운대")
            @RequestParam(value = "keyword", required = false) String keyword,

            @Parameter(description = "지역명 (예: 부산)", example = "부산")
            @RequestParam(value = "region", required = false) String region,

            @Parameter(description = "카테고리 (예: 관광지)", example = "관광지")
            @RequestParam(value = "category", required = false) String category,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CartResponse.TourSearchResponse> response = cartService.searchTours(keyword, region, category, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tour/detail/{contentId}")
    @Operation(summary = "투어 상세 정보 조회", 
               description = "contentId를 통한 투어 상세 정보 조회 (상세 페이지용)")
    public ResponseEntity<CartResponse.TourDetailResponse> getTourDetail(
            @Parameter(description = "투어 컨텐츠 ID", example = "126508")
            @PathVariable String contentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        CartResponse.TourDetailResponse response = cartService.getTourDetail(contentId, 
                userDetails != null ? userDetails.getUsername() : null);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tour/search/theme")
    @Operation(summary = "테마별 투어 검색", 
               description = "테마 기반 투어 검색 (cat1, cat2, cat3 활용)")
    public ResponseEntity<Page<CartResponse.TourSearchResponse>> searchToursByTheme(
            @Parameter(description = "테마 (자연, 인문, 레포츠, 쇼핑, 음식, 숙박, 문화시설, 축제, 체험, 힐링)", example = "자연")
            @RequestParam("theme") String theme,

            @Parameter(description = "지역명 (선택사항)", example = "서울")
            @RequestParam(value = "region", required = false) String region,

            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CartResponse.TourSearchResponse> response = cartService.searchToursByTheme(theme, region, pageable);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cart/tours/simple")
    @Operation(summary = "장바구니에 투어 추가 (contentId만)", 
               description = "프론트에서 contentId만 전송하여 장바구니에 추가",
               security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CartResponse.AddTourResponse> addTourToCartByContentId(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "추가할 투어의 contentId", example = "126508")
            @RequestParam("contentId") String contentId) {
        
        CartResponse.AddTourResponse response = cartService.addTourToCartByContentId(
                userDetails.getUsername(), contentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/places/region")
    @Operation(summary = "지역별 장소 검색", description = "법정동 코드로 해당 지역의 장소들 검색")
    public ResponseEntity<Page<CartResponse.TourSearchResponse>> getPlacesByRegion(
            HttpServletRequest request,
            @Parameter(description = "법정동 시/도 코드 (강원특별자치도=51)", example = "51") 
            @RequestParam String lDongRegnCd,
            @Parameter(description = "법정동 시 코드 (속초시=210)", example = "210") 
            @RequestParam String lDongSignguCd,
            @Parameter(description = "페이지 번호", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") 
            @RequestParam(defaultValue = "20") int size) {
        
        // 조회수 증가 (비동기 처리)
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        regionService.incrementViewCountByLDong(lDongRegnCd, lDongSignguCd, ipAddress, userAgent);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CartResponse.TourSearchResponse> response = cartService.searchPlacesByLDong(lDongRegnCd, lDongSignguCd, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/places/region/theme")
    @Operation(summary = "지역 및 테마별 장소 검색", description = "법정동 코드와 contentTypeId로 장소들 검색")
    public ResponseEntity<Page<CartResponse.TourSearchResponse>> getPlacesByRegionAndTheme(
            HttpServletRequest request,
            @Parameter(description = "법정동 시/도 코드 (강원특별자치도=51)", example = "51") 
            @RequestParam String lDongRegnCd,
            @Parameter(description = "법정동 시 코드 (속초시=210)", example = "210") 
            @RequestParam String lDongSignguCd,
            @Parameter(description = "컨텐츠 타입 ID (12=관광지, 15=축제공연행사, 28=레포츠, 32=숙박, 39=음식점)", example = "12") 
            @RequestParam Integer contentTypeId,
            @Parameter(description = "페이지 번호", example = "0") 
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") 
            @RequestParam(defaultValue = "20") int size) {
        
        // 조회수 증가 (비동기 처리)
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        regionService.incrementViewCountByLDong(lDongRegnCd, lDongSignguCd, ipAddress, userAgent);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CartResponse.TourSearchResponse> response = cartService.searchPlacesByLDongAndContentType(lDongRegnCd, lDongSignguCd, contentTypeId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 유틸리티 메서드: IP 주소 추출
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    @PostMapping("/carts")
    @Operation(summary = "새 장바구니 생성", description = "사용자의 새 장바구니 생성",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CartResponse.CartDetailResponse> createCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "법정동 시/도 코드", example = "11")
            @RequestParam(value = "lDongRegnCd", defaultValue = "11") String lDongRegnCd,
            @Parameter(description = "법정동 시 코드", example = "110")
            @RequestParam(value = "lDongSignguCd", defaultValue = "110") String lDongSignguCd) {
        CartResponse.CartDetailResponse response = cartService.createCart(userDetails.getUsername(), lDongRegnCd, lDongSignguCd);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/carts")
    @Operation(summary = "사용자의 모든 장바구니 조회", description = "사용자의 모든 장바구니 목록 조회",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<List<CartResponse.CartDetailResponse>> getUserCarts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<CartResponse.CartDetailResponse> response = cartService.getUserCarts(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/carts/{cartId}")
    @Operation(summary = "특정 장바구니 조회", description = "cartId로 특정 장바구니 조회",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CartResponse.CartDetailResponse> getCartById(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장바구니 ID")
            @PathVariable UUID cartId) {
        CartResponse.CartDetailResponse response = cartService.getCartById(userDetails.getUsername(), cartId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/carts/{cartId}")
    @Operation(summary = "장바구니 삭제", description = "특정 장바구니와 그 안의 모든 투어 삭제",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Void> deleteCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장바구니 ID")
            @PathVariable UUID cartId) {
        cartService.deleteCart(userDetails.getUsername(), cartId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/carts/{cartId}/tours")
    @Operation(summary = "특정 장바구니에 투어 추가", description = "지정된 장바구니에 새로운 투어 추가",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CartResponse.AddTourResponse> addTourToSpecificCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장바구니 ID")
            @PathVariable UUID cartId,
            @RequestBody CartResponse.TourSearchResponse tourResponse) {
        CartResponse.AddTourResponse response = cartService.addTourToSpecificCart(userDetails.getUsername(), cartId, tourResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/carts/{cartId}/tours/simple")
    @Operation(summary = "특정 장바구니에 투어 추가 (contentId만)", 
               description = "지정된 장바구니에 contentId로 투어 추가",
               security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CartResponse.AddTourResponse> addTourToSpecificCartByContentId(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장바구니 ID")
            @PathVariable UUID cartId,
            @Parameter(description = "추가할 투어의 contentId", example = "126508")
            @RequestParam("contentId") String contentId) {
        
        CartResponse.AddTourResponse response = cartService.addTourToSpecificCartByContentId(
                userDetails.getUsername(), cartId, contentId);
        return ResponseEntity.ok(response);
    }
}