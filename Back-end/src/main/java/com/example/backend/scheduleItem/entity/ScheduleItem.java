package com.example.backend.scheduleItem.entity;

import com.example.backend.schedule.entity.Schedule;
import com.example.backend.tour.entity.Tour;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigInteger;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "ScheduleItem")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class ScheduleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "schedule_item_id", columnDefinition = "BINARY(16)")
    private UUID scheduleItemId;

    @Column(name = "content_id" , nullable = false, length = 100)
    private String contentId;

    @Column(name = "day_number", nullable = true)
    private int dayNumber;

    @Column(name = "memo", length = 100, nullable = true)
    private String memo;

    @Column(name = "cost", nullable = false)
    private int cost;

    @Column(name = "\"order\"", nullable = true)
    private int order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule scheduleId;
}
