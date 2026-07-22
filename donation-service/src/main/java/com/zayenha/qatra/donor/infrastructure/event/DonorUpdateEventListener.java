package com.zayenha.qatra.donor.infrastructure.event;

import com.zayenha.qatra._shared.event.GDPRDeletionRequestedEvent;
import com.zayenha.qatra._shared.event.UserEmailVerifiedEvent;
import com.zayenha.qatra._shared.event.UserSignUpEvent;
import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.user.api.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DonorUpdateEventListener {

    private final DonorCommandUseCases donorCommandUseCases;

    @EventListener
    public void onUserLoggedIn(UserLoggedInEvent event) {
        donorCommandUseCases.reactivateDonor(event.getUserId());
    }
    @EventListener
    public void onDeletionRequested(GDPRDeletionRequestedEvent event) {
        donorCommandUseCases.requestDeletion(event.userId());
    }
    @EventListener
    public void onEmailVerified(UserEmailVerifiedEvent event) {
        donorCommandUseCases.activateProfile(event.userId());
    }
    @EventListener
    public void onDonorSignUp(UserSignUpEvent event) {
        donorCommandUseCases.updateProfile(event.userId());
    }
}
