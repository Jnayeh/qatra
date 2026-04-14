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

    @Value("${kafka.topic.notification-dispatch.name:notification.dispatch}")
    private String notificationDispatchTopicName;
    @Value("${kafka.topic.notification-dispatch.partitions:3}")
    private int notificationDispatchPartitions;
    @Value("${kafka.topic.notification-dispatch.replicas:1}")
    private int notificationDispatchReplicas;

    @Value("${kafka.topic.emergency-created.name:emergency.created}")
    private String emergencyCreatedTopicName;
    @Value("${kafka.topic.emergency-created.partitions:3}")
    private int emergencyCreatedPartitions;
    @Value("${kafka.topic.emergency-created.replicas:1}")
    private int emergencyCreatedReplicas;

    @Value("${kafka.topic.appointment-reminder.name:appointment.reminder}")
    private String appointmentReminderTopicName;
    @Value("${kafka.topic.appointment-reminder.partitions:3}")
    private int appointmentReminderPartitions;
    @Value("${kafka.topic.appointment-reminder.replicas:1}")
    private int appointmentReminderReplicas;

    @Value("${kafka.topic.eligibility-restored.name:eligibility.restored}")
    private String eligibilityRestoredTopicName;
    @Value("${kafka.topic.eligibility-restored.partitions:3}")
    private int eligibilityRestoredPartitions;
    @Value("${kafka.topic.eligibility-restored.replicas:1}")
    private int eligibilityRestoredReplicas;

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

    @Bean
    public NewTopic notificationDispatchTopic() {
        return TopicBuilder.name(notificationDispatchTopicName)
                .partitions(notificationDispatchPartitions).replicas(notificationDispatchReplicas).build();
    }

    @Bean
    public NewTopic emergencyCreatedTopic() {
        return TopicBuilder.name(emergencyCreatedTopicName)
                .partitions(emergencyCreatedPartitions).replicas(emergencyCreatedReplicas).build();
    }

    @Bean
    public NewTopic appointmentReminderTopic() {
        return TopicBuilder.name(appointmentReminderTopicName)
                .partitions(appointmentReminderPartitions).replicas(appointmentReminderReplicas).build();
    }

    @Bean
    public NewTopic eligibilityRestoredTopic() {
        return TopicBuilder.name(eligibilityRestoredTopicName)
                .partitions(eligibilityRestoredPartitions).replicas(eligibilityRestoredReplicas).build();
    }
}
