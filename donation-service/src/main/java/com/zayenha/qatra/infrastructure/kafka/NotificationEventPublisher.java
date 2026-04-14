package com.zayenha.qatra.infrastructure.kafka;

import com.zayenha.qatra._shared.event.AppointmentReminderEvent;
import com.zayenha.qatra._shared.event.EligibilityRestoredEvent;
import com.zayenha.qatra._shared.event.EmergencyCreatedEvent;
import com.zayenha.qatra._shared.event.NotificationDispatchEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final EventPublisher eventPublisher;

    @Value("${kafka.topic.notification-dispatch.name:notification.dispatch}")
    private String notificationDispatchTopic;

    @Value("${kafka.topic.emergency-created.name:emergency.created}")
    private String emergencyCreatedTopic;

    @Value("${kafka.topic.appointment-reminder.name:appointment.reminder}")
    private String appointmentReminderTopic;

    @Value("${kafka.topic.eligibility-restored.name:eligibility.restored}")
    private String eligibilityRestoredTopic;

    public void publishEmergencyCreated(Long emergencyId, List<Long> matchedDonorIds) {
        var event = new EmergencyCreatedEvent(
            emergencyId, matchedDonorIds,
            UUID.randomUUID().toString(), Instant.now());
        eventPublisher.publish(emergencyCreatedTopic, "emerg-notif-" + emergencyId, event);
    }

    public void publishAppointmentReminder(Long appointmentId, Long donorId, String slotTime) {
        var event = new AppointmentReminderEvent(
            appointmentId, donorId, slotTime,
            UUID.randomUUID().toString(), Instant.now());
        eventPublisher.publish(appointmentReminderTopic, "appt-remind-" + appointmentId, event);
    }

    public void publishEligibilityRestored(Long donorId, String eligibleFromDate) {
        var event = new EligibilityRestoredEvent(
            donorId, eligibleFromDate,
            UUID.randomUUID().toString(), Instant.now());
        eventPublisher.publish(eligibilityRestoredTopic, "elig-" + donorId, event);
    }

    public void publishNotificationDispatch(Long userId, String type, String title, String body, String data) {
        var event = new NotificationDispatchEvent(
            userId, type, title, body, data,
            UUID.randomUUID().toString(), Instant.now());
        eventPublisher.publish(notificationDispatchTopic, "notif-" + userId, event);
    }
}
