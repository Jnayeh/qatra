package com.zayenha.qatra.appointment.application;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra._shared.exception.ValidationException;
import com.zayenha.qatra.appointment.domain.exception.AppointmentErrorCode;
import com.zayenha.qatra.appointment.domain.model.*;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentCommandUseCases;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentQueryUseCases;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService implements AppointmentCommandUseCases, AppointmentQueryUseCases {

    private final AppointmentRepositoryPort repository;

    @Override
    @Transactional
    public Appointment book(Long donorId, Long slotId) {
        if (repository.existsByDonorIdAndStatusIn(donorId,
                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CHECKED_IN, AppointmentStatus.IN_SCREENING))) {
            throw new ConflictException("Donor already has an active appointment", AppointmentErrorCode.DONOR_ALREADY_BOOKED.name());
        }
        var appointment = new Appointment(donorId, slotId, null, null, null, null);
        return repository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment checkIn(Long appointmentId) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ValidationException("Only scheduled appointments can be checked in",
                    AppointmentErrorCode.INVALID_APPOINTMENT_STATUS.name());
        }
        appointment.checkIn();
        return repository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment startScreening(Long appointmentId) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() != AppointmentStatus.CHECKED_IN) {
            throw new ValidationException("Only checked-in appointments can start screening",
                    AppointmentErrorCode.INVALID_APPOINTMENT_STATUS.name());
        }
        appointment.startScreening();
        return repository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment complete(Long appointmentId, DonationOutcome outcome, String notes) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("Appointment already completed",
                    AppointmentErrorCode.APPOINTMENT_ALREADY_COMPLETED.name());
        }
        appointment.complete(outcome, notes);
        return repository.save(appointment);
    }

    @Override
    @Transactional
    public Appointment cancel(Long appointmentId) {
        var appointment = findOrThrow(appointmentId);
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new ValidationException("Cannot cancel a completed appointment",
                    AppointmentErrorCode.APPOINTMENT_CANNOT_BE_CANCELLED.name());
        }
        appointment.cancel();
        return repository.save(appointment);
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
        return repository.saveScreening(screening);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Appointment> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
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
    public Optional<HealthScreening> findScreeningByAppointmentId(Long appointmentId) {
        return repository.findScreeningByAppointmentId(appointmentId);
    }

    private Appointment findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Appointment not found: " + id,
                        AppointmentErrorCode.APPOINTMENT_NOT_FOUND.name()));
    }
}
