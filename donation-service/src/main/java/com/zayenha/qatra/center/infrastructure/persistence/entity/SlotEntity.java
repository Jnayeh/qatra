package com.zayenha.qatra.center.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "slots")
@Getter
@Setter
@NoArgsConstructor
public class SlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "slot_seq")
    @SequenceGenerator(name = "slot_seq", sequenceName = "slot_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_id", nullable = false)
    private CenterEntity center;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "max_bookings", nullable = false)
    private int maxBookings;

    @Column(name = "max_regular_bookings", nullable = false)
    private int maxRegularBookings;

    @Column(name = "booked_count", nullable = false)
    private int bookedCount;

    @Column(name = "regular_booked_count", nullable = false)
    private int regularBookedCount;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
