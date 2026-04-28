package com.zayenha.qatra.system.application;

import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.domain.port.out.GDPRRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GDPRService {

    private final GDPRRepositoryPort repository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditPublisher auditPublisher;

    @Transactional
    public GDPRDeletionRequest requestDeletion(Long userId, String reason) {
        var existing = repository.findByUserId(userId);
        if (existing.isPresent() && existing.get().getStatus() == GDPRDeletionStatus.IN_PROGRESS) {
            return existing.get();
        }
        var saved = repository.save(new GDPRDeletionRequest(userId, reason));
        auditPublisher.publish("GDPR_DELETION_REQUESTED", saved.getId(), "GDPRDeletionRequest", null, Map.of("userId", userId, "reason", reason));
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
