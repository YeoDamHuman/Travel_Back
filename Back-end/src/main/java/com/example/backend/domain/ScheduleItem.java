package com.example.backend.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;
import java.sql.Time;
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

    @Column(name = "place_id" , nullable = false, columnDefinition = "BINARY(16)")
    private UUID placeId;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @Column(name = "start_time", nullable = false)
    private Time startTime;

    @Column(name = "end_time", nullable = false)
    private Time endTime;

    @Column(name = "memo", length = 100)
    private String memo;

    @Column(name = "cost", nullable = false)
    private BigInteger cost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule scheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cartId;

}
