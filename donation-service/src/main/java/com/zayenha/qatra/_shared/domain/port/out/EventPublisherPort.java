package com.zayenha.qatra._shared.domain.port.out;

import java.util.List;
import java.util.Map;

public interface EventPublisherPort {

    void publishEmergencyCreated(Long emergencyId, List<Long> matchedDonorIds);

    void publishAppointmentReminder(Long appointmentId, Long donorId, String slotTime);

    void publishEligibilityRestored(Long donorId, String eligibleFromDate);

    void publishNotificationDispatch(Long userId, String email, String type, String title, String body, Map<String, Object> data);
}
