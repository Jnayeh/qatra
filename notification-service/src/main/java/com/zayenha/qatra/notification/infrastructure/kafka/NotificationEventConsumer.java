package com.zayenha.qatra.notification.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zayenha.qatra.notification.application.dto.AppointmentReminderEvent;
import com.zayenha.qatra.notification.application.dto.EligibilityReminderEvent;
import com.zayenha.qatra.notification.application.dto.EligibilityRestoredEvent;
import com.zayenha.qatra.notification.application.dto.EmailVerificationEvent;
import com.zayenha.qatra.notification.application.dto.EmergencyCreatedEvent;
import com.zayenha.qatra.notification.application.dto.PasswordResetEvent;
import com.zayenha.qatra.notification.application.dto.ProfileCompletionNudgeEvent;
import com.zayenha.qatra.notification.application.service.NotificationDispatchService;
import com.zayenha.qatra.notification.domain.model.NotificationChannel;
import com.zayenha.qatra.notification.domain.model.NotificationPayload;
import com.zayenha.qatra.notification.domain.model.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationDispatchService dispatchService;
    private final NotificationResultPublisher resultPublisher;
    private final ObjectMapper objectMapper;
    private final String channelConfig;

    public NotificationEventConsumer(
            NotificationDispatchService dispatchService,
            NotificationResultPublisher resultPublisher,
            ObjectMapper objectMapper,
            @Value("${notification.channels:IN_APP,EMAIL}") String channelConfig) {
        this.dispatchService = dispatchService;
        this.resultPublisher = resultPublisher;
        this.objectMapper = objectMapper;
        this.channelConfig = channelConfig;
    }

    @KafkaListener(topics = "#{'${kafka.topic.emergency-created.name:emergency.created}'}")
    public void consumeEmergencyCreated(String message) {
        handle(message, EmergencyCreatedEvent.class, event -> {
            var requestedChannels = mapChannels(event.correlationId(), event.channels());
            for (var userId : event.matchedUserIds()) {
                var html = """
                    <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                        <h2 style="color: #c0392b;">Blood Donation Needed</h2>
                        <p>An emergency blood request has been created in your area and you are a good match.</p>
                        <p style="font-weight: bold;">Please respond immediately.</p>
                    </div>""";
                var payload = new NotificationPayload(
                        userId, null, event.emergencyId(), null,
                        NotificationType.EMERGENCY_ALERT,
                        "Blood Donation Needed",
                        "An emergency blood request has been created in your area and you are a good match. Please respond immediately.",
                        html,
                        Map.of("emergencyId", event.emergencyId()),
                        event.correlationId() + "-" + userId,
                        event.occurredAt(),
                        requestedChannels);
                dispatchService.dispatch(payload, channelConfig);
            }
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.appointment-reminder.name:appointment.reminder}'}")
    public void consumeAppointmentReminder(String message) {
        handle(message, AppointmentReminderEvent.class, event -> {
            var requestedChannels = mapChannels(event.correlationId(), event.channels());
            var html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2980b9;">Appointment Reminder</h2>
                    <p>You have an appointment scheduled at <strong>%s</strong>.</p>
                </div>""".formatted(event.slotTime());
            var payload = new NotificationPayload(
                    event.donorId(), null, null, event.appointmentId(),
                    NotificationType.APPOINTMENT_REMINDER,
                    "Appointment Reminder",
                    "You have an appointment scheduled at " + event.slotTime(),
                    html,
                    Map.of("appointmentId", event.appointmentId()),
                    event.correlationId(), event.occurredAt(),
                    requestedChannels);
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.eligibility-restored.name:eligibility.restored}'}")
    public void consumeEligibilityRestored(String message) {
        handle(message, EligibilityRestoredEvent.class, event -> {
            var requestedChannels = mapChannels(event.correlationId(), event.channels());
            var html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #27ae60;">You Can Donate Again</h2>
                    <p>Your eligibility to donate blood has been restored. You can now schedule a new donation.</p>
                </div>""";
            var payload = new NotificationPayload(
                    event.userId(), null, null, null,
                    NotificationType.ELIGIBILITY_REMINDER,
                    "You Can Donate Again",
                    "Your eligibility to donate blood has been restored. You can now schedule a new donation.",
                    html,
                    Map.of("eligibleFromDate", event.eligibleFromDate()),
                    event.correlationId(), event.occurredAt(),
                    requestedChannels);
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.eligibility-reminder.name:eligibility.reminder}'}")
    public void consumeEligibilityReminder(String message) {
        handle(message, EligibilityReminderEvent.class, event -> {
            var requestedChannels = mapChannels(event.correlationId(), event.channels());
            var html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #27ae60;">You Can Donate Again</h2>
                    <p>Your eligibility to donate blood has been restored. Schedule your next donation today!</p>
                </div>""";
            var payload = new NotificationPayload(
                    event.userId(), null, null, null,
                    NotificationType.ELIGIBILITY_REMINDER,
                    "You Can Donate Again",
                    "Your eligibility to donate blood has been restored. Schedule your next donation today!",
                    html,
                    Map.of("eligibleFromDate", event.eligibleFromDate()),
                    event.correlationId(), event.occurredAt(),
                    requestedChannels);
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.password-reset.name:password.reset}'}")
    public void consumePasswordReset(String message) {
        handleWithResult(message, PasswordResetEvent.class, "PasswordResetEvent",
                PasswordResetEvent::userId, PasswordResetEvent::correlationId,
                event -> {
            var requestedChannels = mapChannels(event.correlationId(), event.channels());
            var html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #e67e22;">Password Reset Request</h2>
                    <p>Click the link below to reset your password:</p>
                    <p><a href="%s" style="color: #2980b9;">Reset Password</a></p>
                    <p style="color: #7f8c8d; font-size: 12px;">This link will expire in 1 hour.</p>
                </div>""".formatted(event.resetLink());
            var payload = new NotificationPayload(
                    event.userId(), event.email(), null, null,
                    NotificationType.PASSWORD_RESET,
                    "Password Reset Request",
                    "Click the link to reset your password: " + event.resetLink(),
                    html,
                    Map.of("resetToken", event.resetToken(), "email", event.email()),
                    event.correlationId(), event.occurredAt(),
                    requestedChannels);
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.email-verification.name:email.verification}'}")
    public void consumeEmailVerification(String message) {
        handleWithResult(message, EmailVerificationEvent.class, "EmailVerificationEvent",
                EmailVerificationEvent::userId, EmailVerificationEvent::correlationId,
                event -> {
            var requestedChannels = mapChannels(event.correlationId(), event.channels());
            var html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #2980b9;">Verify Your Email</h2>
                    <p>Click the link below to verify your email address:</p>
                    <p><a href="%s" style="color: #2980b9;">Verify Email</a></p>
                    <p style="color: #7f8c8d; font-size: 12px;">This link will expire in 24 hours.</p>
                </div>""".formatted(event.verificationLink());
            var payload = new NotificationPayload(
                    event.userId(), event.email(), null, null,
                    NotificationType.EMAIL_VERIFICATION,
                    "Verify your email",
                    "Click the link to verify your email: " + event.verificationLink(),
                    html,
                    Map.of("verificationToken", event.verificationToken(), "email", event.email()),
                    event.correlationId(), event.occurredAt(),
                    requestedChannels);
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    @KafkaListener(topics = "#{'${kafka.topic.profile-completion-nudge.name:profile.completion.nudge}'}")
    public void consumeProfileCompletionNudge(String message) {
        handle(message, ProfileCompletionNudgeEvent.class, event -> {
            var requestedChannels = mapChannels(event.correlationId(), event.channels());
            var html = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2 style="color: #8e44ad;">Complete Your Profile</h2>
                    <p>Complete your donor profile to become eligible for blood donation requests.</p>
                </div>""";
            var payload = new NotificationPayload(
                    event.userId(), null, null, null,
                    NotificationType.PROFILE_COMPLETION,
                    "Complete Your Profile",
                    "Complete your donor profile to become eligible for blood donation requests.",
                    html,
                    Map.of(),
                    event.correlationId(), event.occurredAt(),
                    requestedChannels);
            dispatchService.dispatch(payload, channelConfig);
        });
    }

    private List<NotificationChannel> mapChannels(String correlation, List<String> channels) {
        if (channels == null || channels.isEmpty()) {
            log.info("Event {} has no channels configured", correlation);
            return List.of();
        }
        return channels.stream().map(String::toUpperCase)
                .map(NotificationChannel::valueOf)
                .collect(Collectors.toList());
    }

    @FunctionalInterface
    private interface EventAction<T> {
        void accept(T event) throws Exception;
    }

    @FunctionalInterface
    private interface UserIdExtractor<T> {
        Long extract(T event);
    }

    @FunctionalInterface
    private interface CorrelationExtractor<T> {
        String extract(T event);
    }

    private <T> void handle(String message, Class<T> type, EventAction<T> action) {
        try {
            action.accept(objectMapper.readValue(message, type));
        } catch (Exception e) {
            log.error("[NOTIFICATION] Failed to process {} event: {}", type.getSimpleName(), e.getMessage(), e);
        }
    }

    private <T> void handleWithResult(String message, Class<T> type, String eventType,
                                       UserIdExtractor<T> userIdExtractor,
                                       CorrelationExtractor<T> correlationExtractor,
                                       EventAction<T> action) {
        T event = null;
        try {
            event = objectMapper.readValue(message, type);
            var userId = userIdExtractor.extract(event);
            var corrId = correlationExtractor.extract(event);
            log.info("Received {} for userId={} correlationId={}", eventType, userId, corrId);
            action.accept(event);
            resultPublisher.publishResult(corrId, eventType, userId, "DELIVERY_SUCCESS", null);
            log.info("{} delivered successfully for userId={}", eventType, userId);
        } catch (Exception e) {
            log.error("Failed to process {} event: {}", eventType, e.getMessage(), e);
            if (event != null) {
                resultPublisher.publishResult(
                        correlationExtractor.extract(event), eventType,
                        userIdExtractor.extract(event), "DELIVERY_FAILED", e.getMessage());
            }
        }
    }
}
