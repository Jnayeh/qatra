package com.zayenha.qatra.system.application;

import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.event.GDPRDeletionRequestedEvent;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.system.application.proxy.GDPRUserProxy;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.domain.port.in.GDPRCommandUseCases;
import com.zayenha.qatra.system.domain.port.in.GDPRQueryUseCases;
import com.zayenha.qatra.system.domain.port.out.GDPRRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GDPRService implements GDPRCommandUseCases, GDPRQueryUseCases {

    private final GDPRRepositoryPort repository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditPublisher auditPublisher;
    private final GDPRUserProxy userProxy;

    @Transactional
    public GDPRDeletionRequest requestDeletion(Long userId, String reason) {
        var user = userProxy.getUser(AuditUtils.currentUserId());
        var roles = user.roles().stream().map(Enum::name).toList();
        if (!roles.contains("SUPER_ADMIN") && !Objects.equals(user.id(), userId)) {
            throw new AccessDeniedException("You are not allowed to request deletion for this user");
        }
        var existing = repository.findByUserId(userId);
        if (existing.isPresent() && existing.get().getStatus() == GDPRDeletionStatus.IN_PROGRESS) {
            return existing.get();
        }
        userProxy.requestDeletion(userId);
        var saved = repository.save(new GDPRDeletionRequest(userId, reason));
        auditPublisher.publish("GDPR_DELETION_REQUESTED", saved.getId(), "GDPRDeletionRequest", null, Map.of("userId", userId, "reason", reason));
        eventPublisher.publishEvent(new GDPRDeletionRequestedEvent(userId, reason, Instant.now()));
        return saved;
    }

    @Transactional
    public GDPRDeletionRequest complete(Long requestId) {
        var request = findOrThrow(requestId);
        var oldStatus = request.getStatus();
        request.complete();
        var saved = repository.save(request);
        auditPublisher.publish("GDPR_DELETION_COMPLETED", requestId, "GDPRDeletionRequest",
            Map.of("status", oldStatus.name()),
            Map.of("status", GDPRDeletionStatus.COMPLETED.name()));
        return saved;
    }

    @Transactional
    public GDPRDeletionRequest cancel(Long requestId) {
        var request = findOrThrow(requestId);
        var oldStatus = request.getStatus();
        request.cancel();
        var saved = repository.save(request);
        auditPublisher.publish("GDPR_DELETION_CANCELLED", requestId, "GDPRDeletionRequest",
            Map.of("status", oldStatus.name()),
            Map.of("status", GDPRDeletionStatus.CANCELED.name()));
        return saved;
    }

    @Transactional(readOnly = true)
    public GDPRDeletionRequest findById(Long id) {
        return findOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<GDPRDeletionRequest> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<GDPRDeletionRequest> findByStatus(GDPRDeletionStatus status) {
        return repository.findByStatus(status);
    }

    private GDPRDeletionRequest findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Deletion request not found: " + id, "GDPR_NOT_FOUND"));
    }
}
