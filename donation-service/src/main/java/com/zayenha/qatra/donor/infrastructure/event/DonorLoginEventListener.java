package com.zayenha.qatra.donor.infrastructure.event;

import com.zayenha.qatra.donor.domain.port.in.DonorCommandUseCases;
import com.zayenha.qatra.user.api.UserLoggedInEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DonorLoginEventListener {

    private final DonorCommandUseCases donorCommandUseCases;

    @EventListener
    public void onUserLoggedIn(UserLoggedInEvent event) {
        donorCommandUseCases.reactivateDonor(event.getUserId());
    }
}
