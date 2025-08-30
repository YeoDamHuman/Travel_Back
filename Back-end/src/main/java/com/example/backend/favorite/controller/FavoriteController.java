package com.example.backend.favorite.controller;

import com.example.backend.favorite.dto.request.FavoriteRequest;
import com.example.backend.favorite.dto.response.FavoriteResponse;
import com.example.backend.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/favorites")
@Tag(name = "FavoriteAPI", description = "즐겨찾기 API")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "즐겨찾기 목록 조회", description = "사용자의 즐겨찾기 장소 목록 조회",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<FavoriteResponse.FavoriteListResponse> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {
        FavoriteResponse.FavoriteListResponse response = favoriteService.getFavorites(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/toggle")
    @Operation(summary = "즐겨찾기 토글", description = "장소를 즐겨찾기에 추가/제거 토글",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<FavoriteResponse.FavoriteActionResponse> toggleFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody FavoriteRequest.AddFavoriteRequest request) {
        FavoriteResponse.FavoriteActionResponse response = favoriteService.toggleFavorite(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{contentId}")
    @Operation(summary = "즐겨찾기 제거", description = "장소를 즐겨찾기에서 제거",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<FavoriteResponse.FavoriteActionResponse> removeFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장소 contentId", example = "126508")
            @PathVariable String contentId) {
        FavoriteResponse.FavoriteActionResponse response = favoriteService.removeFavorite(userDetails.getUsername(), contentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/{contentId}")
    @Operation(summary = "즐겨찾기 상태 확인", description = "특정 장소의 즐겨찾기 상태 확인",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<Boolean> checkFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "장소 contentId", example = "126508")
            @PathVariable String contentId) {
        boolean isFavorite = favoriteService.isFavorite(userDetails.getUsername(), contentId);
        return ResponseEntity.ok(isFavorite);
    }
}