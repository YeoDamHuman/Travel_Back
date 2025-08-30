package com.example.backend.region.repository;

import com.example.backend.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    
    Optional<Region> findByRegionCode(String regionCode);
    Optional<Region> findByRegionName(String regionName);
}