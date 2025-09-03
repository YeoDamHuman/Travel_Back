package com.example.backend.region.repository;

import com.example.backend.region.entity.Region;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    
    Optional<Region> findByRegionCode(String regionCode);
    Optional<Region> findByRegionName(String regionName);
    
    @Query("SELECT r FROM Region r WHERE r.lDongRegnCd = :lDongRegnCd AND r.lDongSignguCd = :lDongSignguCd")
    List<Region> findByLDongRegnCdAndLDongSignguCd(@Param("lDongRegnCd") String lDongRegnCd, @Param("lDongSignguCd") String lDongSignguCd);

    /**
     * 핫플 지역 조회 (조회수 기준 상위 N개)
     */
    @Query("SELECT r FROM Region r ORDER BY r.viewCount DESC, r.regionName ASC")
    List<Region> findTopRegionsByViewCount(Pageable pageable);

    /**
     * 핫플 지역 조회 - 시 단위만 (군 단위 제외)
     */
    @Query("SELECT r FROM Region r WHERE r.regionName LIKE '%시' ORDER BY r.viewCount DESC, r.regionName ASC")
    List<Region> findTopCitiesByViewCount(Pageable pageable);

    /**
     * 전체 지역 조회 - 시 단위만 (군 단위 제외)
     */
    @Query("SELECT r FROM Region r WHERE r.regionName LIKE '%시' ORDER BY r.regionName ASC")
    List<Region> findAllCities();

    /**
     * 동시성 처리를 위한 비관적 락으로 지역 조회
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Region r WHERE r.regionCode = :regionCode")
    Optional<Region> findByRegionCodeWithLock(@Param("regionCode") String regionCode);

    /**
     * 조회수 증가 (원자적 연산)
     */
    @Modifying
    @Query("UPDATE Region r SET r.viewCount = r.viewCount + 1, r.lastViewedAt = CURRENT_TIMESTAMP WHERE r.regionCode = :regionCode")
    int incrementViewCountByRegionCode(@Param("regionCode") String regionCode);

    /**
     * 법정동 코드 기반 조회수 증가 (원자적 연산)
     */
    @Modifying
    @Query("UPDATE Region r SET r.viewCount = r.viewCount + 1, r.lastViewedAt = CURRENT_TIMESTAMP WHERE r.lDongRegnCd = :lDongRegnCd AND r.lDongSignguCd = :lDongSignguCd")
    int incrementViewCountByLDong(@Param("lDongRegnCd") String lDongRegnCd, @Param("lDongSignguCd") String lDongSignguCd);


    /**
     * "lDongRegnCd_lDongSignguCd" 형태로 조합된 여러 코드 키에 해당하는 모든 지역 정보를 조회합니다.
     * @param concatenatedCodes 조회할 조합 코드 목록
     * @return Region 리스트
     */
    @Query("SELECT r FROM Region r WHERE CONCAT(r.lDongRegnCd, '_', r.lDongSignguCd) IN :codes")
    List<Region> findByConcatenatedCodesIn(@Param("codes") Collection<String> concatenatedCodes);
}