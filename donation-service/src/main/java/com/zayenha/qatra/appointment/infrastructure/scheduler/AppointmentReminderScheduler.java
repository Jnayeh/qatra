package com.zayenha.qatra.appointment.infrastructure.scheduler;

import com.zayenha.qatra._shared.domain.DateUtils;
import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentQueryUseCases;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import io.micrometer.observation.annotation.Observed;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    private final AppointmentQueryUseCases queryUseCases;
    private final EventPublisherPort eventPublisher;

    @Observed
    @Scheduled(cron = "${appointment.reminder.cron:0 0 8 * * *}")
    @Transactional(readOnly = true)
    public void sendAppointmentReminders() {
        var today = DateUtils.utcToday();
        var appointments = queryUseCases.findScheduledAppointmentsByDate(today);

        for (var appointment : appointments) {
            var slotTime = appointment.getSlotDate() + " " + appointment.getSlotTime();
            eventPublisher.publishAppointmentReminder(appointment.getId(), appointment.getDonorId(), slotTime);
            log.info("Sent appointment reminder for appointmentId={}, userId={}", appointment.getId(), appointment.getDonorId());
        }
        if (!appointments.isEmpty()) {
            log.info("AppointmentReminderScheduler: sent {} reminders", appointments.size());
        }
    }
}
