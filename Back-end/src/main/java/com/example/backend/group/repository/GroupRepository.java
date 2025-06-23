package com.example.backend.group.repository;

import com.example.backend.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {
    // 그룹과 해당 그룹에 속한 유저들을 함께 조회하는 쿼리
    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.users WHERE g.groupId = :groupId")
    Optional<Group> findByIdWithUsers(@Param("groupId") UUID groupId);
}
