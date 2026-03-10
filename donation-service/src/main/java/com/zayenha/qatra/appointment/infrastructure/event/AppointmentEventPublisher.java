package com.zayenha.qatra.appointment.infrastructure.event;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.infrastructure.kafka.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentEventPublisher {

    private static final String TOPIC = "appointment-events";

    private final EventPublisher eventPublisher;

    public void appointmentCreated(Appointment appointment) {
        var event = new AppointmentEvent(
            appointment.getId(), appointment.getDonorId(), appointment.getSlotId(),
            appointment.getCenterId(), appointment.getStatus(), appointment.getOutcome(), "CREATED");
        eventPublisher.publish(TOPIC, "appt-" + appointment.getId(), event);
    }

    public void appointmentCheckedIn(Appointment appointment) {
        var event = new AppointmentEvent(
            appointment.getId(), appointment.getDonorId(), appointment.getSlotId(),
            appointment.getCenterId(), appointment.getStatus(), appointment.getOutcome(), "CHECKED_IN");
        eventPublisher.publish(TOPIC, "appt-" + appointment.getId(), event);
    }

    public void appointmentCompleted(Appointment appointment) {
        var event = new AppointmentEvent(
            appointment.getId(), appointment.getDonorId(), appointment.getSlotId(),
            appointment.getCenterId(), appointment.getStatus(), appointment.getOutcome(), "COMPLETED");
        eventPublisher.publish(TOPIC, "appt-" + appointment.getId(), event);
    }

    public void appointmentCancelled(Appointment appointment) {
        var event = new AppointmentEvent(
            appointment.getId(), appointment.getDonorId(), appointment.getSlotId(),
            appointment.getCenterId(), appointment.getStatus(), appointment.getOutcome(), "CANCELLED");
        eventPublisher.publish(TOPIC, "appt-" + appointment.getId(), event);
    }
}
