package com.zayenha.qatra.system.application;

import com.zayenha.qatra._shared.event.AuditEvent;
import com.zayenha.qatra._shared.event.AuditUtils;
import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.domain.port.out.GDPRRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GDPRService {

    private final GDPRRepositoryPort repository;
    private final ApplicationEventPublisher eventPublisher;

    private void audit(String action, Long entityId, String oldValue, String newValue) {
        eventPublisher.publishEvent(new AuditEvent(AuditUtils.currentUserId(), action, "GDPRDeletionRequest", entityId, oldValue, newValue, null, null));
    }

    @Transactional
    public GDPRDeletionRequest requestDeletion(Long userId, String reason) {
        var existing = repository.findByUserId(userId);
        if (existing.isPresent() && existing.get().getStatus() == GDPRDeletionStatus.PENDING) {
            return existing.get();
        }
        var saved = repository.save(new GDPRDeletionRequest(userId, reason));
        audit("GDPR_DELETION_REQUESTED", saved.getId(), null, "userId=" + userId);
        return saved;
    }

    @Transactional
    public GDPRDeletionRequest approve(Long requestId, String processedBy) {
        var request = findOrThrow(requestId);
        var oldStatus = request.getStatus();
        request.approve(processedBy);
        var saved = repository.save(request);
        audit("GDPR_DELETION_APPROVED", requestId, "status=" + oldStatus, "processedBy=" + processedBy);
        return saved;
    }

    @Transactional
    public GDPRDeletionRequest reject(Long requestId, String processedBy) {
        var request = findOrThrow(requestId);
        var oldStatus = request.getStatus();
        request.reject(processedBy);
        var saved = repository.save(request);
        audit("GDPR_DELETION_REJECTED", requestId, "status=" + oldStatus, "processedBy=" + processedBy);
        return saved;
    }

    @Transactional
    public GDPRDeletionRequest complete(Long requestId) {
        var request = findOrThrow(requestId);
        var oldStatus = request.getStatus();
        request.complete();
        var saved = repository.save(request);
        audit("GDPR_DELETION_COMPLETED", requestId, "status=" + oldStatus, "");
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
