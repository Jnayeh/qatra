package com.zayenha.qatra.user.infrastructure.scheduler;

import com.zayenha.qatra.user.domain.port.out.SessionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupScheduler {

    private final SessionRepositoryPort sessionRepository;

    @Scheduled(cron = "${session.cleanup.cron:0 0 3 * * *}")
    @Transactional
    public void cleanExpiredSessions() {
        var cutoff = Instant.now();
        sessionRepository.deleteExpiredSessions(cutoff);
        log.info("SessionCleanupScheduler: purged sessions expired before {}", cutoff);
    }
}
