package com.zayenha.qatra.appointment.application.proxy;

import com.zayenha.qatra.user.api.UserApi;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AptUserProxy {

    private final UserApi userApi;

    public UserEntity getUserReference(Long id) {
        return userApi.getUserReference(id);
    }

    public String getUserDisplayName(Long userId) {
        return getUserReference(userId).getDisplayName();
    }
}
