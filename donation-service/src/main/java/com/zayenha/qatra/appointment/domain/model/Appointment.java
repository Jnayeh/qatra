package com.zayenha.qatra.appointment.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class Appointment {
    private Long id;
    private Long donorId;
    private Long slotId;
    private Long centerId;
    private AppointmentStatus status;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Instant checkInTime;
    private Instant completedAt;
    private DonationOutcome outcome;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public Appointment() {}

    public Appointment(Long donorId, Long slotId, Long centerId, LocalDate appointmentDate,
                       LocalTime startTime, LocalTime endTime) {
        this.donorId = donorId;
        this.slotId = slotId;
        this.centerId = centerId;
        this.appointmentDate = appointmentDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public void checkIn() {
        this.status = AppointmentStatus.CHECKED_IN;
        this.checkInTime = Instant.now();
    }

    public void startScreening() {
        this.status = AppointmentStatus.IN_SCREENING;
    }

    public void complete(DonationOutcome outcome, String notes) {
        this.status = AppointmentStatus.COMPLETED;
        this.outcome = outcome;
        this.notes = notes;
        this.completedAt = Instant.now();
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
    }
}
