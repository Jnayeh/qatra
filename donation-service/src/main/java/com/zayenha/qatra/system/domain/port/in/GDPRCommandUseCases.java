package com.zayenha.qatra.system.domain.port.in;

import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;

public interface GDPRCommandUseCases {

    GDPRDeletionRequest requestDeletion(Long userId, String reason);

    GDPRDeletionRequest complete(Long requestId);

    GDPRDeletionRequest cancel(Long requestId);
}
