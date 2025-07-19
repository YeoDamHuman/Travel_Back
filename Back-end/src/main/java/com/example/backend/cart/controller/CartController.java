package com.example.backend.cart.controller;

import com.example.backend.cart.dto.request.AddTourRequest;
import com.example.backend.cart.dto.response.CartResponse;
import com.example.backend.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
@Tag(name = "CartAPI", description = "장바구니 API")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "장바구니 조회", description = "사용자의 장바구니 내용 조회",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CartResponse.CartDetailResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        CartResponse.CartDetailResponse response = cartService.getCart(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tours")
    @Operation(summary = "장바구니에 투어 추가", description = "장바구니에 새로운 투어 추가",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<CartResponse.AddTourResponse> addTourToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddTourRequest request) {
        CartResponse.AddTourResponse response = cartService.addTourToCart(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tours/{tourId}")
    @Operation(summary = "장바구니에서 투어 삭제",
            description = "장바구니에서 특정 투어 삭제",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Void> removeTourFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable UUID tourId) {
        cartService.removeTourFromCart(userDetails.getUsername(), tourId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/tours")
    @Operation(summary = "장바구니 전체 비우기",
            description = "장바구니의 모든 투어 삭제",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}