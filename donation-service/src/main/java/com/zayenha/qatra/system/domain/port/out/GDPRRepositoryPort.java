package com.zayenha.qatra.system.domain.port.out;

import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;

import java.util.List;
import java.util.Optional;

public interface GDPRRepositoryPort {
    GDPRDeletionRequest save(GDPRDeletionRequest request);
    Optional<GDPRDeletionRequest> findById(Long id);
    Optional<GDPRDeletionRequest> findByUserId(Long userId);
    List<GDPRDeletionRequest> findByStatus(GDPRDeletionStatus status);
    List<GDPRDeletionRequest> findAll();
    PageResult<GDPRDeletionRequest> findAll(SearchCriteria criteria, GDPRDeletionStatus status);
}
