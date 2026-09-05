package com.zayenha.qatra.system.infrastructure.event;

import com.zayenha.qatra._shared.event.GDPRDeletionRequestedEvent;
import com.zayenha.qatra._shared.event.UserEmailVerifiedEvent;
import com.zayenha.qatra._shared.event.UserSignUpEvent;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.system.domain.model.GDPRDeletionRequest;
import com.zayenha.qatra.system.domain.model.GDPRDeletionStatus;
import com.zayenha.qatra.system.domain.port.in.GDPRCommandUseCases;
import com.zayenha.qatra.system.domain.port.out.GDPRRepositoryPort;
import com.zayenha.qatra.system.infrastructure.persistence.repository.GDPRDeletionRequestJpaRepository;
import com.zayenha.qatra.user.api.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestUpdateEventListener {

    private final GDPRCommandUseCases gdprCommandUseCases;
    private final GDPRRepositoryPort repository;

    @EventListener
    public void onUserLoggedIn(UserLoggedInEvent event) {
        repository.findByUserId(event.getUserId()).ifPresent(gdprDeletionRequest -> {
            gdprCommandUseCases.cancel(gdprDeletionRequest.getId(), event.getUserId());
        });
    }

}
