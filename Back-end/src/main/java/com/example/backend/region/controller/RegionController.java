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

    @GetMapping("/{regionCode}")
    @Operation(summary = "특정 지역 정보 조회", description = "지역 코드로 특정 지역 정보 조회",
            security = @SecurityRequirement(name = "JWT"))
    public ResponseEntity<RegionResponse.RegionInfo> getRegionByCode(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "지역 코드", example = "1") 
            @PathVariable String regionCode) {
        RegionResponse.RegionInfo response = regionService.getRegionByCode(regionCode);
        return ResponseEntity.ok(response);
    }
}