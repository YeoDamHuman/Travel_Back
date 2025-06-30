package com.example.backend.cart.controller;

import com.example.backend.cart.dto.request.CartRequest;
import com.example.backend.cart.dto.response.CartResponse;
import com.example.backend.cart.service.CartService;
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

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
@Tag(name = "CartAPI", description = "장바구니 및 투어 관리 API")
@Slf4j
public class CartController {

    private final CartService cartService;

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
            @RequestBody CartRequest.AddTourRequest request) {
        CartResponse.AddTourResponse response = cartService.addTourToCart(userDetails.getUsername(), request);
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
}