package com.zayenha.qatra._config.kafka;

import com.zayenha.qatra._shared.domain.port.out.EventPublisherPort;
import com.zayenha.qatra._shared.event.AppointmentReminderEvent;
import com.zayenha.qatra._shared.event.EligibilityReminderEvent;
import com.zayenha.qatra._shared.event.EligibilityRestoredEvent;
import com.zayenha.qatra._shared.event.EmergencyCreatedEvent;
import com.zayenha.qatra._shared.event.EmailVerificationEvent;
import com.zayenha.qatra._shared.event.PasswordResetEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher implements EventPublisherPort {

    private final EventPublisher eventPublisher;

    @Value("${kafka.topic.emergency-created.name:emergency.created}")
    private String emergencyCreatedTopic;

    @Value("${kafka.topic.appointment-reminder.name:appointment.reminder}")
    private String appointmentReminderTopic;

    @Value("${kafka.topic.eligibility-restored.name:eligibility.restored}")
    private String eligibilityRestoredTopic;

    @Value("${kafka.topic.eligibility-reminder.name:eligibility.reminder}")
    private String eligibilityReminderTopic;

    @Value("${kafka.topic.password-reset.name:password.reset}")
    private String passwordResetTopic;

    @Value("${kafka.topic.email-verification.name:email.verification}")
    private String emailVerificationTopic;

    @Value("${notification.emergency-channels:IN_APP}")
    private List<String> emergencyChannels;

    @Value("${notification.appointment-channels:IN_APP}")
    private List<String> appointmentChannels;

    @Value("${notification.eligibility-channels:IN_APP,EMAIL}")
    private List<String> eligibilityChannels;

    @Value("${notification.password-reset-channels:IN_APP,EMAIL}")
    private List<String> passwordResetChannels;

    @Value("${notification.email-verification-channels:IN_APP,EMAIL}")
    private List<String> emailVerificationChannels;

    @Override
    public void publishEmergencyCreated(Long emergencyId, List<Long> matchedDonorIds) {
        var event = new EmergencyCreatedEvent(
            emergencyId, matchedDonorIds,
            UUID.randomUUID().toString(), Instant.now(),
            emergencyChannels);
        eventPublisher.publish(emergencyCreatedTopic, "emerg-notif-" + emergencyId, event);
    }

    @Override
    public void publishAppointmentReminder(Long appointmentId, Long donorId, String slotTime) {
        var event = new AppointmentReminderEvent(
            appointmentId, donorId, slotTime,
            UUID.randomUUID().toString(), Instant.now(),
            appointmentChannels);
        eventPublisher.publish(appointmentReminderTopic, "appt-remind-" + appointmentId, event);
    }

    @Override
    public void publishEligibilityRestored(Long donorId, String eligibleFromDate) {
        var event = new EligibilityRestoredEvent(
            donorId, eligibleFromDate,
            UUID.randomUUID().toString(), Instant.now(),
            eligibilityChannels);
        eventPublisher.publish(eligibilityRestoredTopic, "elig-" + donorId, event);
    }

    @Override
    public void publishEligibilityReminder(Long donorId, String eligibleFromDate) {
        var event = new EligibilityReminderEvent(
            donorId, eligibleFromDate,
            UUID.randomUUID().toString(), Instant.now(),
            eligibilityChannels);
        eventPublisher.publish(eligibilityReminderTopic, "elig-remind-" + donorId, event);
    }

    @Override
    public void publishPasswordReset(Long userId, String email, String resetToken, String resetLink) {
        var event = new PasswordResetEvent(
            userId, email, resetToken, resetLink,
            UUID.randomUUID().toString(), Instant.now(),
            passwordResetChannels);
        eventPublisher.publish(passwordResetTopic, "pwd-reset-" + userId, event);
    }

    @Override
    public void publishEmailVerification(Long userId, String email, String verificationToken, String verificationLink) {
        var event = new EmailVerificationEvent(
            userId, email, verificationToken, verificationLink,
            UUID.randomUUID().toString(), Instant.now(),
            emailVerificationChannels);
        eventPublisher.publish(emailVerificationTopic, "email-verify-" + userId, event);
    }
}
