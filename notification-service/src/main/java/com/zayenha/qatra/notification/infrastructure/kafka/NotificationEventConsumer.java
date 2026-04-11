package com.zayenha.qatra.notification.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zayenha.qatra.notification.application.dto.AppointmentReminderEvent;
import com.zayenha.qatra.notification.application.dto.EligibilityRestoredEvent;
import com.zayenha.qatra.notification.application.dto.EmergencyCreatedEvent;
import com.zayenha.qatra.notification.application.dto.NotificationDispatchEvent;
import com.zayenha.qatra.notification.application.service.NotificationDispatchService;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationDispatchService dispatchService;
    private final ObjectMapper objectMapper;
    private final String channelConfig;

    public NotificationEventConsumer(
            NotificationDispatchService dispatchService,
            @Value("${notification.channels:IN_APP,EMAIL}") String channelConfig) {
        this.dispatchService = dispatchService;
        this.channelConfig = channelConfig;
        this.objectMapper = new ObjectMapper()
                .findAndRegisterModules();
    }

    @KafkaListener(topics = "#{'${kafka.topic.notification-dispatch.name:notification.dispatch}'}")
    public void consumeNotificationDispatch(String message) {
        try {
            var event = objectMapper.readValue(message, NotificationDispatchEvent.class);
            var payload = new NotificationPayload(
                    event.userId(), event.type(), event.title(),
                    event.body(), event.data(),
                    event.correlationId(), event.occurredAt());
            dispatchService.dispatch(payload, channelConfig);
        } catch (Exception e) {
            log.error("Failed to process notification.dispatch event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "#{'${kafka.topic.emergency-created.name:emergency.created}'}")
    public void consumeEmergencyCreated(String message) {
        try {
            var event = objectMapper.readValue(message, EmergencyCreatedEvent.class);
            for (var donorId : event.matchedDonorIds()) {
                var payload = new NotificationPayload(
                        donorId, "EMERGENCY_CREATED",
                        "Urgent: Blood Donation Needed",
                        "An emergency blood request has been created in your area. Please respond.",
                        "{\"emergencyId\":" + event.emergencyId() + "}",
                        event.correlationId() + "-" + donorId,
                        event.occurredAt());
                dispatchService.dispatch(payload, channelConfig);
            }
        } catch (Exception e) {
            log.error("Failed to process emergency.created event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "#{'${kafka.topic.appointment-reminder.name:appointment.reminder}'}")
    public void consumeAppointmentReminder(String message) {
        try {
            var event = objectMapper.readValue(message, AppointmentReminderEvent.class);
            var payload = new NotificationPayload(
                    event.donorId(), "APPOINTMENT_REMINDER",
                    "Appointment Reminder",
                    "You have an appointment scheduled at " + event.slotTime(),
                    "{\"appointmentId\":" + event.appointmentId() + "}",
                    event.correlationId(), event.occurredAt());
            dispatchService.dispatch(payload, channelConfig);
        } catch (Exception e) {
            log.error("Failed to process appointment.reminder event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "#{'${kafka.topic.eligibility-restored.name:eligibility.restored}'}")
    public void consumeEligibilityRestored(String message) {
        try {
            var event = objectMapper.readValue(message, EligibilityRestoredEvent.class);
            var payload = new NotificationPayload(
                    event.donorId(), "ELIGIBILITY_RESTORED",
                    "You Can Donate Again",
                    "Your eligibility to donate blood has been restored. You can now schedule a new donation.",
                    "{\"eligibleFromDate\":\"" + event.eligibleFromDate() + "\"}",
                    event.correlationId(), event.occurredAt());
            dispatchService.dispatch(payload, channelConfig);
        } catch (Exception e) {
            log.error("Failed to process eligibility.restored event: {}", e.getMessage(), e);
        }
    }
}
