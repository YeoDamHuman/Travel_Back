package com.example.backend.region.service;

import com.example.backend.region.dto.response.RegionResponse;
import com.example.backend.region.entity.Region;
import com.example.backend.region.entity.RegionViewLog;
import com.example.backend.region.repository.RegionRepository;
import com.example.backend.region.repository.RegionViewLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegionService {

    private final RegionRepository regionRepository;
    private final RegionViewLogRepository regionViewLogRepository;
    private final RegionInitService regionInitService;
    
    private static final int VIEW_COUNT_COOLDOWN_SECONDS = 10; // 10초 쿨다운 (개발용)
    private static final int HOT_REGION_LIMIT = 10; // 핫플 지역 개수

    @Transactional(readOnly = true)
    public RegionResponse.RegionListResponse getAllRegions() {
        List<Region> regions = regionRepository.findAllCities();
        
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

    @Transactional(readOnly = true)
    public RegionResponse.RegionListResponse searchRegionsByLDong(String lDongRegnCd, String lDongSignguCd) {
        List<Region> regions = regionRepository.findByLDongRegnCdAndLDongSignguCd(lDongRegnCd, lDongSignguCd);
        
        List<RegionResponse.RegionInfo> regionInfos = regions.stream()
                .map(this::convertToRegionInfo)
                .collect(Collectors.toList());

        return RegionResponse.RegionListResponse.builder()
                .regions(regionInfos)
                .totalCount(regionInfos.size())
                .build();
    }

    /**
     * 지역 조회수 증가 (중복 방지 및 비동기 처리)
     */
    @Async
    @Transactional
    public void incrementViewCount(String regionCode, String ipAddress, String userAgent) {
        try {
            // 쿨다운 시간 내 중복 조회 체크
            LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(VIEW_COUNT_COOLDOWN_SECONDS);
            boolean recentlyViewed = regionViewLogRepository.existsByIpAndRegionAndCreatedAtAfter(
                    ipAddress, regionCode, cutoffTime);
            
            if (recentlyViewed) {
                log.debug("중복 조회 방지: IP {} - 지역 코드 {}", ipAddress, regionCode);
                return;
            }

            // 조회수 증가 (원자적 연산으로 동시성 처리)
            int updatedCount = regionRepository.incrementViewCountByRegionCode(regionCode);
            
            if (updatedCount > 0) {
                // 조회 로그 기록
                RegionViewLog viewLog = RegionViewLog.builder()
                        .ipAddress(ipAddress)
                        .regionCode(regionCode)
                        .userAgent(userAgent)
                        .build();
                regionViewLogRepository.save(viewLog);
                
                log.info("지역 조회수 증가: {} (IP: {})", regionCode, ipAddress);
            } else {
                log.warn("존재하지 않는 지역 코드: {}", regionCode);
            }
            
        } catch (Exception e) {
            log.error("조회수 증가 중 오류 발생: 지역코드={}, IP={}", regionCode, ipAddress, e);
        }
    }

    /**
     * 법정동 코드 기반 지역 조회수 증가 (중복 방지 및 비동기 처리)
     */
    @Async
    @Transactional
    public void incrementViewCountByLDong(String lDongRegnCd, String lDongSignguCd, String ipAddress, String userAgent) {
        log.info("=== incrementViewCountByLDong 호출됨: lDongRegnCd={}, lDongSignguCd={}, IP={} ===", lDongRegnCd, lDongSignguCd, ipAddress);
        
        try {
            String regionKey = lDongRegnCd + lDongSignguCd; // 로그 저장용 키
            
            // 쿨다운 시간 내 중복 조회 체크
            LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(VIEW_COUNT_COOLDOWN_SECONDS);
            boolean recentlyViewed = regionViewLogRepository.existsByIpAndRegionAndCreatedAtAfter(
                    ipAddress, regionKey, cutoffTime);
            
            log.info("쿨다운 체크: cutoffTime={}, recentlyViewed={}", cutoffTime, recentlyViewed);
            
            if (recentlyViewed) {
                log.warn("중복 조회 방지: IP {} - lDong {}/{}", ipAddress, lDongRegnCd, lDongSignguCd);
                return;
            }

            // 조회수 증가 (원자적 연산으로 동시성 처리)
            log.info("Region 데이터베이스에서 viewCount 증가 시도...");
            int updatedCount = regionRepository.incrementViewCountByLDong(lDongRegnCd, lDongSignguCd);
            log.info("업데이트된 행 수: {}", updatedCount);
            
            if (updatedCount > 0) {
                // 조회 로그 기록
                RegionViewLog viewLog = RegionViewLog.builder()
                        .ipAddress(ipAddress)
                        .regionCode(regionKey)
                        .userAgent(userAgent)
                        .build();
                regionViewLogRepository.save(viewLog);
                
                log.info("✅ 지역 조회수 증가 성공: {}/{} (IP: {})", lDongRegnCd, lDongSignguCd, ipAddress);
            } else {
                log.warn("❌ 존재하지 않는 법정동 코드: {}/{}", lDongRegnCd, lDongSignguCd);
            }
            
        } catch (Exception e) {
            log.error("❌ 조회수 증가 중 오류 발생: lDong={}/{}, IP={}", lDongRegnCd, lDongSignguCd, ipAddress, e);
        }
    }

    /**
     * 핫플 지역 추천 (시 단위만, 조회수 기준 상위 N개)
     */
    @Transactional(readOnly = true)
    public RegionResponse.HotRegionListResponse getHotRegions() {
        Pageable pageable = PageRequest.of(0, HOT_REGION_LIMIT);
        List<Region> hotRegions = regionRepository.findTopCitiesByViewCount(pageable);
        
        List<RegionResponse.HotRegionInfo> hotRegionInfos = hotRegions.stream()
                .map(this::convertToHotRegionInfo)
                .collect(Collectors.toList());

        return RegionResponse.HotRegionListResponse.builder()
                .hotRegions(hotRegionInfos)
                .totalCount(hotRegionInfos.size())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 특정 개수의 핫플 지역 조회 (시 단위만)
     */
    @Transactional(readOnly = true)
    public RegionResponse.HotRegionListResponse getHotRegions(int limit) {
        Pageable pageable = PageRequest.of(0, Math.min(limit, 20)); // 최대 20개 제한
        List<Region> hotRegions = regionRepository.findTopCitiesByViewCount(pageable);
        
        List<RegionResponse.HotRegionInfo> hotRegionInfos = hotRegions.stream()
                .map(this::convertToHotRegionInfo)
                .collect(Collectors.toList());

        return RegionResponse.HotRegionListResponse.builder()
                .hotRegions(hotRegionInfos)
                .totalCount(hotRegionInfos.size())
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private RegionResponse.RegionInfo convertToRegionInfo(Region region) {
        return RegionResponse.RegionInfo.builder()
                .regionId(region.getRegionId())
                .regionName(region.getRegionName())
                .regionCode(region.getRegionCode())
                .lDongRegnCd(region.getLDongRegnCd())
                .lDongSignguCd(region.getLDongSignguCd())
                .regionImage(region.getRegionImage())
                .description(region.getDescription())
                .build();
    }

    private RegionResponse.HotRegionInfo convertToHotRegionInfo(Region region) {
        return RegionResponse.HotRegionInfo.builder()
                .regionId(region.getRegionId())
                .regionName(region.getRegionName())
                .regionCode(region.getRegionCode())
                .lDongRegnCd(region.getLDongRegnCd())
                .lDongSignguCd(region.getLDongSignguCd())
                .regionImage(region.getRegionImage())
                .description(region.getDescription())
                .viewCount(region.getViewCount())
                .lastViewedAt(region.getLastViewedAt())
                .build();
    }

    @Transactional
    public void initializeRegions() {
        regionInitService.refreshRegions();
    }

}