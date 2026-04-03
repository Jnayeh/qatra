package com.zayenha.qatra.appointment.application;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.event.AuditEvent;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.appointment.domain.exception.AppointmentErrorCode;
import com.zayenha.qatra.appointment.domain.model.*;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentCommandUseCases;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentQueryUseCases;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

    private void audit(String action, Long entityId, String oldValue, String newValue) {
        eventPublisher.publishEvent(new AuditEvent(AuditUtils.currentUserId(), action, "Appointment", entityId, oldValue, newValue, null, null));
    }

    @Override
    @Transactional
    @CacheEvict(value = {"appointments"}, allEntries = true)
    public Appointment book(Long donorId, Long slotId) {
        if (repository.existsByDonorIdAndStatusIn(donorId,
                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_SCREENING))) {
            throw new ConflictException("Donor already has an active appointment", AppointmentErrorCode.DONOR_ALREADY_BOOKED.name());
        }
        var appointment = new Appointment(donorId, slotId, null, null, null, null);
        var saved = repository.save(appointment);
        audit("APPOINTMENT_BOOKED", saved.getId(), null, "donorId=" + donorId + " slotId=" + slotId);
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"appointments"}, allEntries = true)
    public Appointment checkIn(Long appointmentId) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ValidationException("Only scheduled appointments can be checked in",
                    AppointmentErrorCode.INVALID_APPOINTMENT_STATUS.name());
        }
        var oldStatus = appointment.getStatus();
        appointment.checkIn();
        var saved = repository.save(appointment);
        audit("APPOINTMENT_CHECKED_IN", saved.getId(), "status=" + oldStatus, "");
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"appointments"}, allEntries = true)
    public Appointment startScreening(Long appointmentId) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new ValidationException("Only checked-in appointments can start screening",
                    AppointmentErrorCode.INVALID_APPOINTMENT_STATUS.name());
        }
        var oldStatus = appointment.getStatus();
        appointment.startScreening();
        var saved = repository.save(appointment);
        audit("APPOINTMENT_SCREENING_STARTED", saved.getId(), "status=" + oldStatus, "");
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"appointments"}, allEntries = true)
    public Appointment complete(Long appointmentId, DonationOutcome outcome, String notes) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("Appointment already completed",
                    AppointmentErrorCode.APPOINTMENT_ALREADY_COMPLETED.name());
        }
        var oldStatus = appointment.getStatus();
        appointment.complete(outcome, notes);
        var saved = repository.save(appointment);
        audit("APPOINTMENT_COMPLETED", saved.getId(), "status=" + oldStatus, "outcome=" + outcome);
        return saved;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"appointments"}, allEntries = true)
    public Appointment cancel(Long appointmentId) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("Cannot cancel a completed appointment",
                    AppointmentErrorCode.APPOINTMENT_CANNOT_BE_CANCELLED.name());
        }
        var oldStatus = appointment.getStatus();
        appointment.cancel();
        var saved = repository.save(appointment);
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
    @CacheEvict(value = {"screenings"}, allEntries = true)
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
        audit("SCREENING_SAVED", saved.getId(), null, "appointmentId=" + appointmentId + " eligible=" + eligible);
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "appointments", key = "#id")
    public Optional<Appointment> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "appointments", key = "'donor:' + #donorId")
    public List<Appointment> findByDonorId(Long donorId) {
        return repository.findByDonorId(donorId);
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
    @Cacheable(value = "screenings", key = "#appointmentId")
    public Optional<HealthScreening> findScreeningByAppointmentId(Long appointmentId) {
        return repository.findScreeningByAppointmentId(appointmentId);
    }

    private Appointment findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appointment not found: " + id,
                        AppointmentErrorCode.APPOINTMENT_NOT_FOUND.name()));
    }
}
