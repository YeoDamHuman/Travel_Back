package com.example.backend.schedule.repository;

import com.example.backend.group.entity.Group;
import com.example.backend.schedule.entity.Schedule;
import com.example.backend.schedule.entity.ScheduleType;
import com.example.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {
    List<Schedule> findAllByGroupId(Group groupId);

    List<Schedule> findAllByUserIdAndScheduleType(User userId, ScheduleType scheduleType);

    List<Schedule> findAllByUserId(User userId);
}