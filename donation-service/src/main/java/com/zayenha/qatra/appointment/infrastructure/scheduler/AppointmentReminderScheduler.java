package com.zayenha.qatra.appointment.infrastructure.scheduler;

import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentReminderScheduler {

    @PersistenceContext
    private EntityManager em;

    private final EventPublisherPort eventPublisher;

    @Scheduled(cron = "${appointment.reminder.cron:0 0 8 * * *}")
    @Transactional(readOnly = true)
    public void sendAppointmentReminders() {
        var today = LocalDate.now();
        @SuppressWarnings("unchecked")
        List<Object[]> results = em.createQuery(
            "SELECT a.id, a.donor.id, s.date, s.startTime FROM AppointmentEntity a JOIN a.slot s WHERE a.status = 'SCHEDULED' AND s.date = :targetDate")
            .setParameter("targetDate", today)
            .getResultList();

        for (var row : results) {
            var appointmentId = (Long) row[0];
            var donorId = (Long) row[1];
            var slotDate = (LocalDate) row[2];
            var startTime = (java.time.LocalTime) row[3];
            var slotTime = slotDate + " " + startTime;
            eventPublisher.publishAppointmentReminder(appointmentId, donorId, slotTime);
            log.info("Sent appointment reminder for appointmentId={}, donorId={}", appointmentId, donorId);
        }
        if (!results.isEmpty()) {
            log.info("AppointmentReminderScheduler: sent {} reminders", results.size());
        }
    }
}
