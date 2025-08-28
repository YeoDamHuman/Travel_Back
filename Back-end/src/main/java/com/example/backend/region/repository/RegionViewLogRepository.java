package com.example.backend.region.repository;

import com.example.backend.region.entity.RegionViewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface RegionViewLogRepository extends JpaRepository<RegionViewLog, Long> {

    /**
     * 특정 시간 내에 같은 IP에서 같은 지역을 조회했는지 확인
     */
    @Query("SELECT COUNT(v) > 0 FROM RegionViewLog v " +
           "WHERE v.ipAddress = :ipAddress " +
           "AND v.regionCode = :regionCode " +
           "AND v.createdAt >= :since")
    boolean existsByIpAndRegionAndCreatedAtAfter(
            @Param("ipAddress") String ipAddress,
            @Param("regionCode") String regionCode,
            @Param("since") LocalDateTime since
    );

    /**
     * 오래된 로그 정리 (30일 이전 데이터 삭제)
     */
    @Modifying
    @Query("DELETE FROM RegionViewLog v WHERE v.createdAt < :cutoffDate")
    void deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}