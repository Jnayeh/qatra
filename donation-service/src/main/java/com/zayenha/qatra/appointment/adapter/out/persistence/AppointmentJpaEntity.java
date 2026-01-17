package com.zayenha.qatra.appointment.adapter.out.persistence;

import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.AppointmentType;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.Instant;

@Getter
@Entity
@Table(name = "appointments")
public class AppointmentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_id", nullable = false)
    private Long donorId;

    @Column(name = "center_id", nullable = false)
    private Long centerId;

    @Column(name = "emergency_id")
    private Long emergencyId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "appointment_type", nullable = false)
    private AppointmentType appointmentType;

    @Column(name = "ml_collected")
    private Integer mlCollected;

    private String notes;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "completed_by_staff_id")
    private Long completedByStaffId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    public AppointmentJpaEntity() {}

    public void setId(Long id) { this.id = id; }

    public void setDonorId(Long donorId) { this.donorId = donorId; }

    public void setCenterId(Long centerId) { this.centerId = centerId; }

    public void setEmergencyId(Long emergencyId) { this.emergencyId = emergencyId; }

    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public void setStatus(AppointmentStatus status) { this.status = status; }

    public void setAppointmentType(AppointmentType appointmentType) { this.appointmentType = appointmentType; }

    public void setMlCollected(Integer mlCollected) { this.mlCollected = mlCollected; }

    public void setNotes(String notes) { this.notes = notes; }

    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public void setQrCode(String qrCode) { this.qrCode = qrCode; }

    public void setCompletedByStaffId(Long completedByStaffId) { this.completedByStaffId = completedByStaffId; }

    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }

    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
}
