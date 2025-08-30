package com.example.backend.region.service;

import com.example.backend.region.dto.response.RegionResponse;
import com.example.backend.region.entity.Region;
import com.example.backend.region.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    @Transactional(readOnly = true)
    public RegionResponse.RegionListResponse getAllRegions() {
        List<Region> regions = regionRepository.findAll();
        
        List<RegionResponse.RegionInfo> regionInfos = regions.stream()
                .map(this::convertToRegionInfo)
                .collect(Collectors.toList());

        return RegionResponse.RegionListResponse.builder()
                .regions(regionInfos)
                .totalCount(regionInfos.size())
                .build();
    }

    @Transactional(readOnly = true)
    public RegionResponse.RegionInfo getRegionByCode(String regionCode) {
        Region region = regionRepository.findByRegionCode(regionCode)
                .orElseThrow(() -> new IllegalArgumentException("지역 코드를 찾을 수 없습니다: " + regionCode));
        
        return convertToRegionInfo(region);
    }

    private RegionResponse.RegionInfo convertToRegionInfo(Region region) {
        return RegionResponse.RegionInfo.builder()
                .regionId(region.getRegionId())
                .regionName(region.getRegionName())
                .regionCode(region.getRegionCode())
                .regionImage(region.getRegionImage())
                .description(region.getDescription())
                .build();
    }
}