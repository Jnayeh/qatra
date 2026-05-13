package com.zayenha.qatra.donor.infrastructure.event;

import com.zayenha.qatra._shared.event.GDPRDeletionRequestedEvent;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DonorDeletionEventListener {

    private final DonorCommandUseCases donorCommandUseCases;

    @EventListener
    public void onDeletionRequested(GDPRDeletionRequestedEvent event) {
        donorCommandUseCases.requestDeletion(event.userId());
    }
}
