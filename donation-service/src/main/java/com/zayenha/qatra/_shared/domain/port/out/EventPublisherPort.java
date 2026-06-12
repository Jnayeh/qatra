package com.zayenha.qatra._shared.domain.port.out;

import java.util.List;

public interface EventPublisherPort {

    void publishEmergencyCreated(Long emergencyId, List<Long> matchedUserIds);

    void publishAppointmentReminder(Long appointmentId, Long donorId, String slotTime);

    void publishEligibilityRestored(Long userId, String eligibleFromDate);

    void publishEligibilityReminder(Long userId, String eligibleFromDate);

    void publishPasswordReset(Long userId, String email, String resetToken, String resetLink);

    void publishEmailVerification(Long userId, String email, String verificationToken, String verificationLink);
}
