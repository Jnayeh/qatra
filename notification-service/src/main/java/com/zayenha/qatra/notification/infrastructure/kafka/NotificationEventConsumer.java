package com.zayenha.qatra.notification.infrastructure.kafka;

import tools.jackson.databind.ObjectMapper;
import com.zayenha.qatra.notification.application.dto.AppointmentReminderEvent;
import com.zayenha.qatra.notification.application.dto.EligibilityRestoredEvent;
import com.zayenha.qatra.notification.application.dto.EmergencyCreatedEvent;
import com.zayenha.qatra.notification.application.dto.NotificationDispatchEvent;
import com.zayenha.qatra.notification.application.dto.ProfileCompletionEvent;
import com.zayenha.qatra.notification.application.dto.StaffMessageEvent;
import com.zayenha.qatra.notification.application.service.NotificationDispatchService;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationDispatchService dispatchService;
    private final ObjectMapper objectMapper;
    private final String channelConfig;

    public NotificationEventConsumer(
            NotificationDispatchService dispatchService,
            ObjectMapper objectMapper,
            @Value("${notification.channels:IN_APP,EMAIL}") String channelConfig) {
        this.dispatchService = dispatchService;
        this.objectMapper = objectMapper;
        this.channelConfig = channelConfig;
    }

    @KafkaListener(topics = "#{'${kafka.topic.notification-dispatch.name:notification.dispatch}'}")
    public void consumeNotificationDispatch(String message) {
        handle(message, NotificationDispatchEvent.class, event -> {
            var payload = new NotificationPayload(
                    event.userId(), event.email(), event.emergencyId(), event.appointmentId(),
                    event.type(), event.channel(),
                    event.title(), event.body(), event.data(),
                    event.correlationId(), event.occurredAt());
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.emergency-created.name:emergency.created}'}")
    public void consumeEmergencyCreated(String message) {
        handle(message, EmergencyCreatedEvent.class, event -> {
            for (var donorId : event.matchedDonorIds()) {
                var payload = new NotificationPayload(
                        donorId, null, event.emergencyId(), null,
                        NotificationType.EMERGENCY_ALERT, NotificationChannel.IN_APP,
                        "Urgent: Blood Donation Needed",
                        "An emergency blood request has been created in your area. Please respond.",
                        Map.of("emergencyId", event.emergencyId()),
                        event.correlationId() + "-" + donorId,
                        event.occurredAt());
                dispatchService.dispatch(payload, channelConfig);
            }
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.appointment-reminder.name:appointment.reminder}'}")
    public void consumeAppointmentReminder(String message) {
        handle(message, AppointmentReminderEvent.class, event -> {
            var payload = new NotificationPayload(
                    event.donorId(), null, null, event.appointmentId(),
                    NotificationType.APPOINTMENT_REMINDER, NotificationChannel.IN_APP,
                    "Appointment Reminder",
                    "You have an appointment scheduled at " + event.slotTime(),
                    Map.of("appointmentId", event.appointmentId()),
                    event.correlationId(), event.occurredAt());
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.eligibility-restored.name:eligibility.restored}'}")
    public void consumeEligibilityRestored(String message) {
        handle(message, EligibilityRestoredEvent.class, event -> {
            var payload = new NotificationPayload(
                    event.donorId(), null, null, null,
                    NotificationType.ELIGIBILITY_REMINDER, NotificationChannel.EMAIL,
                    "You Can Donate Again",
                    "Your eligibility to donate blood has been restored. You can now schedule a new donation.",
                    Map.of("eligibleFromDate", event.eligibleFromDate()),
                    event.correlationId(), event.occurredAt());
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.profile-completion.name:profile.completion}'}")
    public void consumeProfileCompletion(String message) {
        handle(message, ProfileCompletionEvent.class, event -> {
            var payload = new NotificationPayload(
                    event.userId(), event.email(), null, null,
                    NotificationType.PROFILE_COMPLETION, NotificationChannel.EMAIL,
                    "Complete Your Donor Profile",
                    event.message(),
                    Map.of("profileId", event.profileId()),
                    event.correlationId(), event.occurredAt());
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.staff-message.name:staff.message}'}")
    public void consumeStaffMessage(String message) {
        handle(message, StaffMessageEvent.class, event -> {
            var payload = new NotificationPayload(
                    event.userId(), event.email(), null, null,
                    NotificationType.STAFF_MESSAGE, NotificationChannel.IN_APP,
                    event.subject(),
                    event.body(),
                    Map.of("staffId", event.staffId()),
                    event.correlationId(), event.occurredAt());
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @FunctionalInterface
    private interface EventAction<T> {
        void accept(T event) throws Exception;
    }

    private <T> void handle(String message, Class<T> type, EventAction<T> action) {
        try {
            action.accept(objectMapper.readValue(message, type));
        } catch (Exception e) {
            log.error("Failed to process {} event: {}", type.getSimpleName(), e.getMessage(), e);
        }
    }
}
