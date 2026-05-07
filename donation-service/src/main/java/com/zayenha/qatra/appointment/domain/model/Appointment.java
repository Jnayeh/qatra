package com.zayenha.qatra.appointment.domain.model;

import com.zayenha.qatra._shared.domain.BloodType;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class Appointment {
    private Long id;
    private Long slotId;
    private Long donorId;
    private Long centerId;
    private Long emergencyId;
    private Long completedByStaffId;
    private AppointmentStatus status;
    private AppointmentType appointmentType;
    private BloodType bloodType;
    private Integer mlCollected;
    private String notes;
    private String qrCode;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant checkedInAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant cancelledAt;
    private String cancellationReason;
    private DonationOutcome outcome;

    public Appointment() {}

    public Appointment(Long donorId, Long slotId, Long centerId, Long emergencyId,
                       AppointmentType appointmentType, BloodType bloodType) {
        this.donorId = donorId;
        this.slotId = slotId;
        this.centerId = centerId;
        this.emergencyId = emergencyId;
        this.appointmentType = appointmentType;
        this.bloodType = bloodType;
        this.status = AppointmentStatus.SCHEDULED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void checkIn() {
        this.status = AppointmentStatus.CHECKED_IN;
        this.checkedInAt = Instant.now();
    }

    public void startScreening() {
        this.status = AppointmentStatus.IN_SCREENING;
        this.startedAt = Instant.now();
    }

    public void complete(DonationOutcome outcome, String notes) {
        this.status = AppointmentStatus.COMPLETED;
        this.outcome = outcome;
        this.notes = notes;
        this.completedAt = Instant.now();
    }

    public void complete(DonationOutcome outcome, String notes,
                         Integer mlCollected, Long staffId) {
        complete(outcome, notes);
        this.mlCollected = mlCollected;
        this.completedByStaffId = staffId;
    }

    public void markNoShow() {
        this.status = AppointmentStatus.NO_SHOW;
        this.cancelledAt = Instant.now();
    }

    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public void cancel(String reason) {
        cancel();
        this.cancellationReason = reason;
    }

    public void reschedule(Long newSlotId) {
        this.slotId = newSlotId;
        this.status = AppointmentStatus.RESCHEDULED;
        this.updatedAt = Instant.now();
    }
}
