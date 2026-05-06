package com.zayenha.qatra.appointment.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.exception.*;
import com.zayenha.qatra.appointment.application.proxy.AptCenterProxy;
import com.zayenha.qatra.appointment.application.proxy.AptDonorProxy;
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
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService implements AppointmentCommandUseCases, AppointmentQueryUseCases {

    private final AppointmentRepositoryPort repository;
    private final AptCenterProxy centerProxy;
    private final AptDonorProxy donorProxy;
    private final ApplicationEventPublisher eventPublisher;
    private final EventPublisherPort eventPublisherPort;
    private final CacheService cacheService;
    private final AuditPublisher auditPublisher;

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
        auditPublisher.publish("APPOINTMENT_BOOKED", saved.getId(), "Appointment", null,
            Map.of("donorId", donorId, "slotId", slotId, "appointmentType",
                   saved.getAppointmentType() != null ? saved.getAppointmentType().name() : null));
        eventPublisherPort.publishAppointmentReminder(saved.getId(), donorId, null);
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
        auditPublisher.publish("APPOINTMENT_CHECKED_IN", saved.getId(), "Appointment",
            Map.of("status", oldStatus.name()),
            Map.of("status", AppointmentStatus.CHECKED_IN.name(), "donorId", saved.getDonorId()));
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
        auditPublisher.publish("APPOINTMENT_SCREENING_STARTED", saved.getId(), "Appointment",
            Map.of("status", oldStatus.name()),
            Map.of("status", AppointmentStatus.IN_SCREENING.name(), "donorId", saved.getDonorId()));
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

        // Update donor profile stats
        donorProxy.findDonorByUserId(saved.getDonorId()).ifPresent(dto -> {
            if (outcome == DonationOutcome.COMPLETED) {
                dto.setTotalDonations(dto.getTotalDonations() + 1);
                dto.setLastDonationDate(LocalDate.now());
                dto.setEligibleFromDate(LocalDate.now().plusDays(56));
                dto.setReliabilityScore(Math.min(100.0, (dto.getReliabilityScore() != null ? dto.getReliabilityScore() : 50.0) + 2.0));
                dto.setConsecutiveEmergencyDeclines(0);
            }
            dto.setUpdatedAt(java.time.Instant.now());
            donorProxy.saveDonor(dto);
            cacheService.evictByPattern("donorProfiles:*");
        });

        auditPublisher.publish("APPOINTMENT_COMPLETED", saved.getId(), "Appointment",
            Map.of("status", oldStatus.name()),
            Map.of("status", AppointmentStatus.COMPLETED.name(), "outcome", outcome.name(),
                   "donorId", saved.getDonorId(), "mlCollected", saved.getMlCollected()));
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

        // Release slot capacity
        if (saved.getSlotId() != null) {
            centerProxy.findSlotById(saved.getSlotId()).ifPresent(slot -> {
                slot.setBookedCount(Math.max(0, slot.getBookedCount() - 1));
                if (saved.getAppointmentType() == AppointmentType.REGULAR) {
                    slot.setRegularBookedCount(Math.max(0, slot.getRegularBookedCount() - 1));
                }
                centerProxy.updateSlot(slot);
            });
        }

        auditPublisher.publish("APPOINTMENT_CANCELLED", saved.getId(), "Appointment",
            Map.of("status", oldStatus.name()),
            Map.of("status", AppointmentStatus.CANCELLED.name(), "donorId", saved.getDonorId()));
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
        var screening = new HealthScreening(appointmentId, null);
        screening.setWeight(weight);
        screening.setBloodPressure(bloodPressure);
        screening.setHemoglobin(hemoglobin);
        screening.setTemperature(temperature);
        screening.setEligible(eligible);
        screening.setNotes(notes);
        var saved = repository.saveScreening(screening);
        cacheService.evictByPattern("screenings:*");

        var appointment = findOrThrow(appointmentId);
        auditPublisher.publish("SCREENING_SAVED", saved.getId(), "Appointment", null,
            Map.of("appointmentId", appointmentId, "eligible", eligible, "donorId",
                   appointment.getDonorId()));

        if (!eligible) {
            // Auto-cancel appointment and release slot when screening fails
            appointment.cancel("Failed health screening");
            repository.save(appointment);
            if (appointment.getSlotId() != null) {
                centerProxy.findSlotById(appointment.getSlotId()).ifPresent(slot -> {
                    slot.setBookedCount(Math.max(0, slot.getBookedCount() - 1));
                    if (appointment.getAppointmentType() == AppointmentType.REGULAR) {
                        slot.setRegularBookedCount(Math.max(0, slot.getRegularBookedCount() - 1));
                    }
                    centerProxy.updateSlot(slot);
                });
            }
            cacheService.evictByPattern("appointments:*");
            eventPublisherPort.publishEligibilityRestored(appointment.getDonorId(), "deferred");
        } else {
            eventPublisherPort.publishEligibilityRestored(appointment.getDonorId(), "now");
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
