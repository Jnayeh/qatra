package com.zayenha.qatra.appointment.domain.model;

import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;

import java.time.Instant;

public class Appointment {

    private AppointmentId id;
    private final Long donorId;
    private final Long centerId;
    private final Long emergencyId;
    private final Long slotId;
    private AppointmentStatus status;
    private final AppointmentType appointmentType;
    private Integer mlCollected;
    private String notes;
    private String cancellationReason;
    private String qrCode;
    private Long completedByStaffId;
    private final Instant createdAt;
    private Instant confirmedAt;
    private Instant completedAt;
    private Instant cancelledAt;

    private Appointment(Long donorId, Long centerId, Long slotId,
                        AppointmentType appointmentType, Long emergencyId,
                        Instant createdAt) {
        this.donorId = donorId;
        this.centerId = centerId;
        this.slotId = slotId;
        this.appointmentType = appointmentType;
        this.emergencyId = emergencyId;
        this.status = AppointmentStatus.SCHEDULED;
        this.createdAt = createdAt;
    }

    public static Appointment schedule(Long donorId, Long centerId,
                                        Long slotId, AppointmentType appointmentType,
                                        Long emergencyId) {
        return new Appointment(donorId, centerId, slotId, appointmentType, emergencyId, Instant.now());
    }

    public void confirm() {
        if (status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED appointments can be confirmed");
        }
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedAt = Instant.now();
    }

    public void start() {
        if (status != AppointmentStatus.CONFIRMED && status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Only CONFIRMED or SCHEDULED appointments can start");
        }
        this.status = AppointmentStatus.IN_PROGRESS;
    }

    public void complete(Integer mlCollected, Long completedByStaffId) {
        if (status != AppointmentStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS appointments can be completed");
        }
        if (mlCollected == null || mlCollected <= 0) {
            throw new IllegalArgumentException("mlCollected must be positive");
        }
        if (completedByStaffId == null) {
            throw new IllegalArgumentException("completedByStaffId is required");
        }
        this.status = AppointmentStatus.COMPLETED;
        this.mlCollected = mlCollected;
        this.completedByStaffId = completedByStaffId;
        this.completedAt = Instant.now();
    }

    public void cancel(String reason) {
        if (status == AppointmentStatus.COMPLETED || status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel a " + status + " appointment");
        }
        this.status = AppointmentStatus.CANCELLED;
        this.cancellationReason = reason;
        this.cancelledAt = Instant.now();
    }

    public void markNoShow() {
        if (status != AppointmentStatus.CONFIRMED && status != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED or CONFIRMED appointments can be marked as no-show");
        }
        this.status = AppointmentStatus.NO_SHOW;
    }

    public static Appointment reconstruct(AppointmentId id, Long donorId, Long centerId,
                                           Long slotId, AppointmentType appointmentType,
                                           Long emergencyId, AppointmentStatus status,
                                           Integer mlCollected, String notes, String cancellationReason,
                                            String qrCode, Long completedByStaffId,
                                           Instant createdAt, Instant confirmedAt,
                                           Instant completedAt, Instant cancelledAt) {
        Appointment appointment = new Appointment(donorId, centerId, slotId, appointmentType, emergencyId, createdAt);
        appointment.id = id;
        appointment.status = status;
        appointment.mlCollected = mlCollected;
        appointment.notes = notes;
        appointment.cancellationReason = cancellationReason;
        appointment.qrCode = qrCode;
        appointment.completedByStaffId = completedByStaffId;
        appointment.confirmedAt = confirmedAt;
        appointment.completedAt = completedAt;
        appointment.cancelledAt = cancelledAt;
        return appointment;
    }

    void setId(AppointmentId id) { this.id = id; }

    public AppointmentId getId() { return id; }
    public Long getDonorId() { return donorId; }
    public Long getCenterId() { return centerId; }
    public Long getEmergencyId() { return emergencyId; }
    public Long getSlotId() { return slotId; }
    public AppointmentStatus getStatus() { return status; }
    public AppointmentType getAppointmentType() { return appointmentType; }
    public Integer getMlCollected() { return mlCollected; }
    public String getNotes() { return notes; }
    public String getCancellationReason() { return cancellationReason; }
    public String getQrCode() { return qrCode; }
    public Long getCompletedByStaffId() { return completedByStaffId; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getConfirmedAt() { return confirmedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCancelledAt() { return cancelledAt; }
}
