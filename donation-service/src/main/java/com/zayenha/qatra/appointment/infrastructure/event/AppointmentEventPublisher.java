package com.zayenha.qatra.appointment.infrastructure.event;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.infrastructure.kafka.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentEventPublisher {

    @Value("${kafka.topic.appointment-events.name:appointment-events}")
    private String topic;

    private final EventPublisher eventPublisher;

    public void appointmentCreated(Appointment appointment) {
        var event = new AppointmentEvent(
            appointment.getId(), appointment.getDonorId(), appointment.getSlotId(),
            appointment.getCenterId(), appointment.getStatus(), appointment.getOutcome(), "CREATED");
        eventPublisher.publish(topic, "appt-" + appointment.getId(), event);
    }

    public void appointmentCheckedIn(Appointment appointment) {
        var event = new AppointmentEvent(
            appointment.getId(), appointment.getDonorId(), appointment.getSlotId(),
            appointment.getCenterId(), appointment.getStatus(), appointment.getOutcome(), "CHECKED_IN");
        eventPublisher.publish(topic, "appt-" + appointment.getId(), event);
    }

    public void appointmentCompleted(Appointment appointment) {
        var event = new AppointmentEvent(
            appointment.getId(), appointment.getDonorId(), appointment.getSlotId(),
            appointment.getCenterId(), appointment.getStatus(), appointment.getOutcome(), "COMPLETED");
        eventPublisher.publish(topic, "appt-" + appointment.getId(), event);
    }

    public void appointmentCancelled(Appointment appointment) {
        var event = new AppointmentEvent(
            appointment.getId(), appointment.getDonorId(), appointment.getSlotId(),
            appointment.getCenterId(), appointment.getStatus(), appointment.getOutcome(), "CANCELLED");
        eventPublisher.publish(topic, "appt-" + appointment.getId(), event);
    }
}
