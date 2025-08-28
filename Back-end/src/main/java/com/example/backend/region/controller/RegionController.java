package com.example.backend.region.controller;

import com.example.backend.region.dto.response.RegionResponse;
import com.example.backend.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/regions")
@Tag(name = "RegionAPI", description = "지역 정보 API")
public class RegionController {

    private final RegionService regionService;

    @GetMapping
    @Operation(summary = "전체 지역 목록 조회", description = "모든 지역의 이름, 코드, 이미지 정보 조회",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<RegionResponse.RegionListResponse> getAllRegions(
            @AuthenticationPrincipal UserDetails userDetails) {
        RegionResponse.RegionListResponse response = regionService.getAllRegions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "지역 정보 조회", description = "시/도, 시 코드로 지역 정보 조회",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<RegionResponse.RegionListResponse> searchRegions(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "법정동 시/도 코드 (강원특별자치도=51)", example = "51") 
            @RequestParam String lDongRegnCd,
            @Parameter(description = "법정동 시 코드 (속초시=210)", example = "210") 
            @RequestParam String lDongSignguCd) {
        RegionResponse.RegionListResponse response = regionService.searchRegionsByLDong(
                lDongRegnCd, lDongSignguCd);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hot")
    @Operation(summary = "핫플 지역 추천", description = "조회수 기준 인기 지역 상위 10개 추천 (인증 불필요)")
    public ResponseEntity<RegionResponse.HotRegionListResponse> getHotRegions() {
        RegionResponse.HotRegionListResponse response = regionService.getHotRegions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hot/{limit}")
    @Operation(summary = "핫플 지역 추천 (개수 지정)", description = "조회수 기준 인기 지역 상위 N개 추천 (인증 불필요)")
    public ResponseEntity<RegionResponse.HotRegionListResponse> getHotRegions(
            @Parameter(description = "조회할 개수 (최대 20개)", example = "5")
            @PathVariable int limit) {
        RegionResponse.HotRegionListResponse response = regionService.getHotRegions(limit);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/init")
    @Operation(summary = "지역 데이터 수동 초기화", description = "Region 테이블 데이터를 수동으로 초기화 (개발/테스트용)")
    public ResponseEntity<String> initializeRegions() {
        regionService.initializeRegions();
        return ResponseEntity.ok("지역 데이터 초기화 완료");
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
}