package com.zayenha.qatra.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(value = "spring.kafka.bootstrap-servers")
public class KafkaConfig {

    public static final String APPOINTMENT_EVENTS = "appointment-events";
    public static final String EMERGENCY_EVENTS = "emergency-events";
    public static final String AUDIT_EVENTS = "audit-events";

    @Bean
    public NewTopic appointmentEventsTopic() {
        return TopicBuilder.name(APPOINTMENT_EVENTS)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic emergencyEventsTopic() {
        return TopicBuilder.name(EMERGENCY_EVENTS)
                .partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name(AUDIT_EVENTS)
                .partitions(2).replicas(1).build();
    }
}
