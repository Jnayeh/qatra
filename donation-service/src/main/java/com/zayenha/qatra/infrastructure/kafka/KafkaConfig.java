package com.zayenha.qatra.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@ConditionalOnProperty(value = "spring.kafka.bootstrap-servers")
public class KafkaConfig {

    @Value("${kafka.topic.appointment-events.name:appointment-events}")
    private String appointmentTopicName;
    @Value("${kafka.topic.appointment-events.partitions:3}")
    private int appointmentPartitions;
    @Value("${kafka.topic.appointment-events.replicas:1}")
    private int appointmentReplicas;

    @Value("${kafka.topic.emergency-events.name:emergency-events}")
    private String emergencyTopicName;
    @Value("${kafka.topic.emergency-events.partitions:3}")
    private int emergencyPartitions;
    @Value("${kafka.topic.emergency-events.replicas:1}")
    private int emergencyReplicas;

    @Value("${kafka.topic.audit-events.name:audit-events}")
    private String auditTopicName;
    @Value("${kafka.topic.audit-events.partitions:2}")
    private int auditPartitions;
    @Value("${kafka.topic.audit-events.replicas:1}")
    private int auditReplicas;

    @Bean
    public NewTopic appointmentEventsTopic() {
        return TopicBuilder.name(appointmentTopicName)
                .partitions(appointmentPartitions).replicas(appointmentReplicas).build();
    }

    @Bean
    public NewTopic emergencyEventsTopic() {
        return TopicBuilder.name(emergencyTopicName)
                .partitions(emergencyPartitions).replicas(emergencyReplicas).build();
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name(auditTopicName)
                .partitions(auditPartitions).replicas(auditReplicas).build();
    }
}
