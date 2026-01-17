package com.zayenha.qatra.appointment.application.service;

import com.zayenha.qatra.appointment.application.port.in.*;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.AppointmentStatus;
import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentService implements
        ScheduleAppointmentUseCase,
        RescheduleAppointmentUseCase,
        CancelAppointmentUseCase,
        CheckInAppointmentUseCase,
        CompleteAppointmentUseCase,
        MarkNoShowUseCase,
        ViewAppointmentsUseCase,
        AdminDashboardUseCase {

    private final AppointmentRepository appointmentRepository;

    @Override
    public Appointment schedule(ScheduleAppointmentCommand command) {
        Appointment appointment = Appointment.schedule(
                command.donorId(),
                command.centerId(),
                command.slotId(),
                command.appointmentType(),
                command.emergencyId()
        );
        return appointmentRepository.save(appointment);
    }

    @Override
    public void reschedule(RescheduleAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appointment.cancel("Rescheduled to slot " + command.newSlotId());
        appointmentRepository.save(appointment);
    }

    @Override
    public void cancel(CancelAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appointment.cancel(command.reason());
        appointmentRepository.save(appointment);
    }

    @Override
    public void checkIn(CheckInAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appointment.confirm();
        appointment.start();
        appointmentRepository.save(appointment);
    }

    @Override
    public void complete(CompleteAppointmentCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appointment.complete(command.mlCollected(), command.completedByStaffId());
        appointmentRepository.save(appointment);
    }

    @Override
    public void markNoShow(MarkNoShowCommand command) {
        Appointment appointment = appointmentRepository.findById(command.appointmentId())
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
        appointment.markNoShow();
        appointmentRepository.save(appointment);
    }

    @Override
    public List<Appointment> getDonorHistory(Long donorId) {
        return appointmentRepository.findByDonorId(donorId);
    }

    @Override
    public List<Appointment> getDailySchedule(Long centerId, LocalDate date) {
        return appointmentRepository.findByCenterIdAndDate(centerId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public AllAppointmentsResult getAllAppointments(AppointmentsQuery query) {
        List<Appointment> appointments = appointmentRepository.findAll(
                query.centerId(), query.from(), query.to(),
                query.page(), query.size(), query.sortBy(), query.sortDir()
        );
        long total = appointmentRepository.countAll(query.centerId(), query.from(), query.to());
        return new AllAppointmentsResult(appointments, total);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getStats() {
        long completed = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
        long totalMl = appointmentRepository.totalMlCollected();
        long scheduled = appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED);
        long cancelled = appointmentRepository.countByStatus(AppointmentStatus.CANCELLED);
        long noShows = appointmentRepository.countByStatus(AppointmentStatus.NO_SHOW);
        return new DashboardStats(completed, totalMl, scheduled, cancelled, noShows);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CenterDonationSummary> getDonationSummaryByCenter() {
        return appointmentRepository.donationSummaryByCenter()
                .stream()
                .map(row -> new CenterDonationSummary(
                        (Long) row[0],
                        (Long) row[1],
                        (Long) row[2]
                ))
                .toList();
    }

    @Override
    public List<DailyDonationStats> getDailyDonationStats(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return appointmentRepository.dailyDonationStats(start, end)
                .stream()
                .map(row -> new DailyDonationStats(
                        row[0].toString(),
                        (Long) row[1],
                        (Long) row[2]
                ))
                .toList();
    }
}
