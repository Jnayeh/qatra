package com.zayenha.qatra.system.domain.port.in;

import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;

import java.util.List;

public interface GDPRQueryUseCases {

    GDPRDeletionRequest findById(Long id);

    List<GDPRDeletionRequest> findAll();

    List<GDPRDeletionRequest> findByStatus(GDPRDeletionStatus status);
}
