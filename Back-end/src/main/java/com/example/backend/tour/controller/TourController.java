package com.example.backend.tour.controller;

import com.example.backend.tour.dto.response.TourResponse;
import com.example.backend.tour.service.TourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tour")
@Tag(name = "TourAPI", description = "투어 정보 API")
public class TourController {

    private final TourService tourService;

    @GetMapping("/search")
    @Operation(summary = "투어 검색", description = "공공 API 연동을 통한 투어 검색")
    public ResponseEntity<Page<TourResponse>> searchTours(
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
        Page<TourResponse> response = tourService.searchTours(keyword, region, category, pageable);
        return ResponseEntity.ok(response);
    }
}
