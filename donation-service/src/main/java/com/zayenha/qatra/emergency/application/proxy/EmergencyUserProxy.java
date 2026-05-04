package com.zayenha.qatra.emergency.application.proxy;

import com.zayenha.qatra._shared.infrastructure.EntityApi;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmergencyUserProxy {

    private final EntityApi<UserEntity> userApi;

    public UserEntity getUserReference(Long id) {
        return userApi.getReference(id);
    }
}
