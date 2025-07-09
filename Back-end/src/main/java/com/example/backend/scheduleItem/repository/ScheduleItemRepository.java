package com.example.backend.scheduleItem.repository;

import com.example.backend.schedule.entity.Schedule;
import com.example.backend.scheduleItem.entity.ScheduleItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, UUID> {
    List<ScheduleItem> findAllByScheduleId(Schedule schedule);

}
