package com.zayenha.qatra._config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.annotation.EnableKafka;

import java.util.List;

@Configuration
@EnableKafka
@ConditionalOnProperty(value = "spring.kafka.bootstrap-servers")
public class KafkaConfig {

    @Bean
    public List<NewTopic> kafkaTopics(
            @Value("${kafka.topic.appointment-events.name:appointment-events}") String appointmentName,
            @Value("${kafka.topic.appointment-events.partitions:3}") int appointmentPartitions,
            @Value("${kafka.topic.appointment-events.replicas:1}") int appointmentReplicas,
            @Value("${kafka.topic.emergency-events.name:emergency-events}") String emergencyName,
            @Value("${kafka.topic.emergency-events.partitions:3}") int emergencyPartitions,
            @Value("${kafka.topic.emergency-events.replicas:1}") int emergencyReplicas,
            @Value("${kafka.topic.audit-events.name:audit-events}") String auditName,
            @Value("${kafka.topic.audit-events.partitions:2}") int auditPartitions,
            @Value("${kafka.topic.audit-events.replicas:1}") int auditReplicas,
            @Value("${kafka.topic.emergency-created.name:emergency.created}") String emergCreatedName,
            @Value("${kafka.topic.emergency-created.partitions:3}") int emergCreatedPartitions,
            @Value("${kafka.topic.emergency-created.replicas:1}") int emergCreatedReplicas,
            @Value("${kafka.topic.appointment-reminder.name:appointment.reminder}") String apptReminderName,
            @Value("${kafka.topic.appointment-reminder.partitions:3}") int apptReminderPartitions,
            @Value("${kafka.topic.appointment-reminder.replicas:1}") int apptReminderReplicas,
            @Value("${kafka.topic.eligibility-restored.name:eligibility.restored}") String eligName,
            @Value("${kafka.topic.eligibility-restored.partitions:3}") int eligPartitions,
            @Value("${kafka.topic.eligibility-restored.replicas:1}") int eligReplicas,
            @Value("${kafka.topic.eligibility-reminder.name:eligibility.reminder}") String eligReminderName,
            @Value("${kafka.topic.eligibility-reminder.partitions:3}") int eligReminderPartitions,
            @Value("${kafka.topic.eligibility-reminder.replicas:1}") int eligReminderReplicas,
            @Value("${kafka.topic.password-reset.name:password.reset}") String passwordResetName,
            @Value("${kafka.topic.password-reset.partitions:3}") int passwordResetPartitions,
            @Value("${kafka.topic.password-reset.replicas:1}") int passwordResetReplicas,
            @Value("${kafka.topic.email-verification.name:email.verification}") String emailVerificationName,
            @Value("${kafka.topic.email-verification.partitions:3}") int emailVerificationPartitions,
            @Value("${kafka.topic.email-verification.replicas:1}") int emailVerificationReplicas,
            @Value("${kafka.topic.notification-result.name:notification.result}") String notificationResultName,
            @Value("${kafka.topic.notification-result.partitions:3}") int notificationResultPartitions,
            @Value("${kafka.topic.notification-result.replicas:1}") int notificationResultReplicas
    ) {
        return List.of(
            topic(appointmentName, appointmentPartitions, appointmentReplicas),
            topic(emergencyName, emergencyPartitions, emergencyReplicas),
            topic(auditName, auditPartitions, auditReplicas),
            topic(emergCreatedName, emergCreatedPartitions, emergCreatedReplicas),
            topic(apptReminderName, apptReminderPartitions, apptReminderReplicas),
            topic(eligName, eligPartitions, eligReplicas),
            topic(eligReminderName, eligReminderPartitions, eligReminderReplicas),
            topic(passwordResetName, passwordResetPartitions, passwordResetReplicas),
            topic(emailVerificationName, emailVerificationPartitions, emailVerificationReplicas),
            topic(notificationResultName, notificationResultPartitions, notificationResultReplicas)
        );
    }

    private static NewTopic topic(String name, int partitions, int replicas) {
        return TopicBuilder.name(name).partitions(partitions).replicas(replicas).build();
    }
}
