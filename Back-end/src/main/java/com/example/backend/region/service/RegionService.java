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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        Pageable pageable = PageRequest.of(0, limit);
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



    /**
     * 여러 법정동 코드 쌍에 대한 지역명 맵을 일괄 조회합니다. (N+1 문제 해결)
     * @param codePairs 조회할 lDongRegnCd와 lDongSignguCd 코드 쌍 리스트
     * @return Key: "lDongRegnCd_lDongSignguCd", Value: "지역명" 형태의 Map
     */
    @Transactional(readOnly = true)
    public Map<String, String> getRegionNamesByCodePairs(List<CodePair> codePairs) {
        if (codePairs == null || codePairs.isEmpty()) {
            log.warn("getRegionNamesByCodePairs: 입력된 codePairs가 없습니다.");
            return Collections.emptyMap();
        }

        // 💡 1. 서비스에 전달된 입력 값 확인
        log.info("▶️ 1. RegionService 입력값 (codePairs 개수): {}", codePairs.size());
        log.debug("   - codePairs 내용: {}", codePairs);

        // 2. Repository에 전달할 조합 키("1_110" 형태) 목록을 만듭니다.
        List<String> concatenatedCodes = codePairs.stream()
                .map(pair -> pair.lDongRegnCd() + "_" + pair.lDongSignguCd())
                .collect(Collectors.toList());
        log.info("▶️ 2. DB 조회용 키 (concatenatedCodes 개수): {}", concatenatedCodes.size());
        log.debug("   - concatenatedCodes 내용: {}", concatenatedCodes);

        // 3. Repository의 @Query 메서드를 호출하여 한 번에 모든 Region 정보를 가져옵니다.
        List<Region> foundRegions = regionRepository.findByConcatenatedCodesIn(concatenatedCodes);
        log.info("▶️ 3. DB 조회 결과 (foundRegions 개수): {}", foundRegions.size());

        // 조회된 내용이 있다면 첫 번째 결과 샘플을 로그로 남깁니다.
        if (!foundRegions.isEmpty()) {
            log.debug("   - 첫 번째 조회 결과: RegionName={}, lDongRegnCd={}, lDongSignguCd={}",
                    foundRegions.get(0).getRegionName(),
                    foundRegions.get(0).getLDongRegnCd(),
                    foundRegions.get(0).getLDongSignguCd());
        }

        // 4. 결과를 사용하기 쉬운 Map 형태로 변환하여 반환합니다.
        Map<String, String> regionNameMap = foundRegions.stream()
                .collect(Collectors.toMap(
                        region -> region.getLDongRegnCd() + "_" + region.getLDongSignguCd(),
                        Region::getRegionName,
                        (existing, replacement) -> existing
                ));
        log.info("▶️ 4. 최종 반환 Map (regionNameMap 개수): {}", regionNameMap.size());
        log.debug("   - regionNameMap 내용: {}", regionNameMap);

        return regionNameMap;
    }
    /**
     *  ScheduleService에서 코드 쌍을 전달하기 위해 사용할 public record
     */
    public record CodePair(String lDongRegnCd, String lDongSignguCd) {}


    /**
     * 여러 법정동 코드 쌍에 대한 지역 이미지 URL 맵을 일괄 조회합니다. (N+1 문제 해결)
     * @param codePairs 조회할 lDongRegnCd와 lDongSignguCd 코드 쌍 리스트
     * @return Key: "lDongRegnCd_lDongSignguCd", Value: "regionImage URL" 형태의 Map
     */
    @Transactional(readOnly = true)
    public Map<String, String> getRegionImagesByCodePairs(List<CodePair> codePairs) {
        if (codePairs == null || codePairs.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> concatenatedCodes = codePairs.stream()
                .map(pair -> pair.lDongRegnCd() + "_" + pair.lDongSignguCd())
                .collect(Collectors.toList());

        List<Region> foundRegions = regionRepository.findByConcatenatedCodesIn(concatenatedCodes);
        log.info(foundRegions.toString());
        return foundRegions.stream()
                .filter(region -> region.getRegionImage() != null && !region.getRegionImage().isEmpty())
                .collect(Collectors.toMap(
                        region -> region.getLDongRegnCd() + "_" + region.getLDongSignguCd(),
                        Region::getRegionImage,
                        (existing, replacement) -> existing
                ));

    }
}