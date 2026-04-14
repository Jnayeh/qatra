package com.zayenha.qatra.appointment.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditEvent;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra.infrastructure.kafka.NotificationEventPublisher;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.appointment.domain.exception.AppointmentErrorCode;
import com.zayenha.qatra.appointment.domain.model.*;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentCommandUseCases;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentQueryUseCases;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepositoryPort;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService implements AppointmentCommandUseCases, AppointmentQueryUseCases {

    private final AppointmentRepositoryPort repository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;
    private final CacheService cacheService;

    private void audit(String action, Long entityId, String oldValue, String newValue) {
        eventPublisher.publishEvent(new AuditEvent(AuditUtils.currentUserId(), action, "Appointment", entityId, oldValue, newValue, null, null));
    }

    @Override
    @Transactional
    public Appointment book(Long donorId, Long slotId) {
        if (repository.existsByDonorIdAndStatusIn(donorId,
                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_SCREENING))) {
            throw new ConflictException("Donor already has an active appointment", AppointmentErrorCode.DONOR_ALREADY_BOOKED.name());
        }
        var appointment = new Appointment(donorId, slotId, null, null, null, null);
        var saved = repository.save(appointment);
        cacheService.evictByPattern("appointments:*");
        audit("APPOINTMENT_BOOKED", saved.getId(), null, "donorId=" + donorId + " slotId=" + slotId);
        // ponytail: appointment reminder should be scheduled via a cron job closer to the appointment time;
        // slotTime is formatted from available fields — slot look-up from center module would provide full info
        var slotTime = saved.getAppointmentDate() != null
            ? saved.getAppointmentDate().toString() + (saved.getStartTime() != null ? "T" + saved.getStartTime() : "")
            : null;
        notificationEventPublisher.publishAppointmentReminder(saved.getId(), donorId, slotTime);
        return saved;
    }

    @Override
    @Transactional
    public Appointment checkIn(Long appointmentId) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ValidationException("Only scheduled appointments can be checked in",
                    AppointmentErrorCode.INVALID_APPOINTMENT_STATUS.name());
        }
        var oldStatus = appointment.getStatus();
        appointment.checkIn();
        var saved = repository.save(appointment);
        cacheService.evictByPattern("appointments:*");
        audit("APPOINTMENT_CHECKED_IN", saved.getId(), "status=" + oldStatus, "");
        return saved;
    }

    @Override
    @Transactional
    public Appointment startScreening(Long appointmentId) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new ValidationException("Only checked-in appointments can start screening",
                    AppointmentErrorCode.INVALID_APPOINTMENT_STATUS.name());
        }
        var oldStatus = appointment.getStatus();
        appointment.startScreening();
        var saved = repository.save(appointment);
        cacheService.evictByPattern("appointments:*");
        audit("APPOINTMENT_SCREENING_STARTED", saved.getId(), "status=" + oldStatus, "");
        return saved;
    }

    @Override
    @Transactional
    public Appointment complete(Long appointmentId, DonationOutcome outcome, String notes) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("Appointment already completed",
                    AppointmentErrorCode.APPOINTMENT_ALREADY_COMPLETED.name());
        }
        var oldStatus = appointment.getStatus();
        appointment.complete(outcome, notes);
        var saved = repository.save(appointment);
        cacheService.evictByPattern("appointments:*");
        audit("APPOINTMENT_COMPLETED", saved.getId(), "status=" + oldStatus, "outcome=" + outcome);
        return saved;
    }

    @Override
    @Transactional
    public Appointment cancel(Long appointmentId) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("Cannot cancel a completed appointment",
                    AppointmentErrorCode.APPOINTMENT_CANNOT_BE_CANCELLED.name());
        }
        var oldStatus = appointment.getStatus();
        appointment.cancel();
        var saved = repository.save(appointment);
        cacheService.evictByPattern("appointments:*");
        audit("APPOINTMENT_CANCELLED", saved.getId(), "status=" + oldStatus, "");
        return saved;
    }

    @Override
    @Transactional
    public Appointment cancelByDonor(Long appointmentId, Long donorId) {
        var appointment = findOrThrow(appointmentId);
        if (!appointment.getDonorId().equals(donorId)) {
            throw new NotFoundException("Appointment not found", AppointmentErrorCode.APPOINTMENT_NOT_FOUND.name());
        }
        return cancel(appointmentId);
    }

    @Override
    @Transactional
    public HealthScreening saveScreening(Long appointmentId, double weight, String bloodPressure,
                                          double hemoglobin, double temperature, boolean eligible, String notes) {
        var screening = new HealthScreening(appointmentId);
        screening.setWeight(weight);
        screening.setBloodPressure(bloodPressure);
        screening.setHemoglobin(hemoglobin);
        screening.setTemperature(temperature);
        screening.setEligible(eligible);
        screening.setNotes(notes);
        var saved = repository.saveScreening(screening);
        cacheService.evictByPattern("screenings:*");
        audit("SCREENING_SAVED", saved.getId(), null, "appointmentId=" + appointmentId + " eligible=" + eligible);
        // ponytail: eligibility.restored should be triggered by a scheduled job scanning for expired cooldowns;
        // this is a best-effort nudge when a health screening confirms eligibility
        if (eligible) {
            var appointment = findOrThrow(appointmentId);
            notificationEventPublisher.publishEligibilityRestored(appointment.getDonorId(), "now");
        }
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Appointment> findById(Long id) {
        var key = "appointments:" + id;
        var cached = cacheService.get(key, Appointment.class);
        if (cached.isPresent()) return cached;
        var result = repository.findById(id);
        result.ifPresent(r -> cacheService.put(key, r));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByDonorId(Long donorId) {
        var key = "appointments:donor:" + donorId;
        return cacheService.get(key, new TypeReference<List<Appointment>>() {})
                .orElseGet(() -> {
                    var result = repository.findByDonorId(donorId);
                    cacheService.put(key, result);
                    return result;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Appointment> findByCenterIdAndDate(Long centerId, LocalDate date) {
        return repository.findByCenterIdAndDate(centerId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Appointment> findAll(SearchCriteria criteria) {
        return repository.findAll(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<HealthScreening> findScreeningByAppointmentId(Long appointmentId) {
        var key = "screenings:" + appointmentId;
        var cached = cacheService.get(key, HealthScreening.class);
        if (cached.isPresent()) return cached;
        var result = repository.findScreeningByAppointmentId(appointmentId);
        result.ifPresent(r -> cacheService.put(key, r));
        return result;
    }

    private Appointment findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appointment not found: " + id,
                        AppointmentErrorCode.APPOINTMENT_NOT_FOUND.name()));
    }
}
