package com.zayenha.qatra.user.infrastructure.scheduler;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.UserStatus;
import com.zayenha.qatra.user.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GDPRAnonymizationScheduler {

    private static final Duration ANONYMIZATION_DELAY = Duration.ofDays(30);
    private static final String ANONYMIZED_PASSWORD = "UNKNOWN";
    private static final String ANONYMIZED_EMAIL_TEMPLATE = "anonymized-%d@qatra-anonymous.com";

    private final UserRepositoryPort userRepository;
    private final CacheService cacheService;
    private final AuditPublisher auditPublisher;

    @Scheduled(cron = "${gdpr.anonymization.cron:0 0 2 * * *}")
    @Transactional
    public void anonymizeExpiredDeletions() {
        var pendingUsers = userRepository.findByStatus(UserStatus.PENDING_DELETION);
        var now = Instant.now();
        var anonymizedCount = 0;

        for (var user : pendingUsers) {
            if (user.getDeletionRequestedAt() == null
                    || now.isBefore(user.getDeletionRequestedAt().plus(ANONYMIZATION_DELAY))) {
                continue;
            }

            var oldEmail = user.getEmail();
            var oldDisplayName = user.getDisplayName();
            var oldFirstName = user.getFirstName();
            var oldFamilyName = user.getFamilyName();
            var oldPhone = user.getPhone();

            user.setEmail(String.format(ANONYMIZED_EMAIL_TEMPLATE, user.getId()));
            user.setPhone("UNKNOWN");
            user.setHashedPassword(ANONYMIZED_PASSWORD);
            user.setDisplayName("UNKNOWN");
            user.setFirstName("UNKNOWN");
            user.setFamilyName("UNKNOWN");
            user.markDeleted();

            userRepository.save(user);
            cacheService.evictByPattern("users:*");
            cacheService.evictByPattern("userExists:*");

            auditPublisher.publish(
                    user.getId(),
                    "GDPR_ANONYMIZATION_COMPLETED",
                    user.getId(),
                    "User",
                    Map.of(
                            "email", oldEmail,
                            "displayName", oldDisplayName,
                            "firstName", oldFirstName,
                            "familyName", oldFamilyName,
                            "phone", oldPhone
                    ),
                    Map.of(
                            "email", user.getEmail(),
                            "displayName", "UNKNOWN",
                            "status", UserStatus.DELETED.name()
                    ));

            anonymizedCount++;
            log.info("Anonymized user id={}, oldEmail={}", user.getId(), oldEmail);
        }

        log.info("GDPRAnonymizationScheduler: anonymized {} users", anonymizedCount);
    }
}
