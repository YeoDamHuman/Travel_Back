package com.example.backend.schedule.repository;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    /**
     * 특정 사용자가 참여하고 있는 모든 스케줄을 조회합니다.
     * Spring Data JPA의 쿼리 메서드 규칙에 따라 자동으로 구현됩니다.
     * @param user 조회할 사용자 엔티티
     * @return 해당 사용자가 참여하는 스케줄의 리스트
     */
    List<Schedule> findAllByUsersContaining(User user);

    /**
     * 특정 스케줄 ID로 스케줄을 조회할 때, 참여자(users) 목록을 함께 페치 조인하여 조회합니다.
     * N+1 문제를 방지하고 성능을 최적화할 수 있습니다.
     * @param scheduleId 조회할 스케줄의 ID
     * @return 스케줄과 참여자 정보가 포함된 Optional<Schedule>
     */
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.users WHERE s.scheduleId = :scheduleId")
    Optional<Schedule> findWithUsersById(@Param("scheduleId") UUID scheduleId);
}