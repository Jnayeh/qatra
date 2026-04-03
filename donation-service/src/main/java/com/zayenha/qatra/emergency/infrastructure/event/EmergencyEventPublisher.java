package com.zayenha.qatra.emergency.infrastructure.event;

import com.zayenha.qatra.emergency.domain.model.EmergencyRequest;
import com.zayenha.qatra.infrastructure.kafka.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmergencyEventPublisher {

    @Value("${kafka.topic.emergency-events.name:emergency-events}")
    private String topic;

    private final EventPublisher eventPublisher;

    public void emergencyCreated(EmergencyRequest request) {
        var event = new EmergencyEvent(
            request.getId(), request.getBloodType(), request.getUnitsNeeded(),
            request.getUrgency(), request.getStatus(), "CREATED");
        eventPublisher.publish(topic, "emerg-" + request.getId(), event);
    }

    public void emergencyFulfilled(EmergencyRequest request) {
        var event = new EmergencyEvent(
            request.getId(), request.getBloodType(), request.getUnitsNeeded(),
            request.getUrgency(), request.getStatus(), "FULFILLED");
        eventPublisher.publish(topic, "emerg-" + request.getId(), event);
    }

    public void emergencyCancelled(EmergencyRequest request) {
        var event = new EmergencyEvent(
            request.getId(), request.getBloodType(), request.getUnitsNeeded(),
            request.getUrgency(), request.getStatus(), "CANCELLED");
        eventPublisher.publish(topic, "emerg-" + request.getId(), event);
    }
}
