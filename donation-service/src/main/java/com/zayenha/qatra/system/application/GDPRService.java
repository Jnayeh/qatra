package com.zayenha.qatra.system.application;

import com.zayenha.qatra._shared.exception.NotFoundException;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.domain.port.out.GDPRRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GDPRService {

    private final GDPRRepositoryPort repository;

    @Transactional
    public GDPRDeletionRequest requestDeletion(Long userId, String reason) {
        var existing = repository.findByUserId(userId);
        if (existing.isPresent() && existing.get().getStatus() == GDPRDeletionStatus.PENDING) {
            return existing.get();
        }
        return repository.save(new GDPRDeletionRequest(userId, reason));
    }

    @Transactional
    public GDPRDeletionRequest approve(Long requestId, String processedBy) {
        var request = findOrThrow(requestId);
        request.approve(processedBy);
        return repository.save(request);
    }

    @Transactional
    public GDPRDeletionRequest reject(Long requestId, String processedBy) {
        var request = findOrThrow(requestId);
        request.reject(processedBy);
        return repository.save(request);
    }

    @Transactional
    public GDPRDeletionRequest complete(Long requestId) {
        var request = findOrThrow(requestId);
        request.complete();
        return repository.save(request);
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
