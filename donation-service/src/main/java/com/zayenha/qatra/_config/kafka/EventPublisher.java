package com.zayenha.qatra._config.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventPublisher {

    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    public EventPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void publish(String topic, String key, Object event) {
        if (kafkaTemplate == null) {
            log.debug("Kafka not configured, skipping event publication to topic {}", topic);
            return;
        }
        try {
            var payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, payload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("[SAGA] Failed to publish event to topic {}: {}", topic, ex.getMessage(), ex);
                        } else {
                            log.info("[SAGA] Event published successfully to topic={} key={}", topic, key);
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("[SAGA] Failed to serialize event: {}", e.getMessage(), e);
        }
    }

    public void publish(String topic, Object event) {
        publish(topic, null, event);
    }
}
