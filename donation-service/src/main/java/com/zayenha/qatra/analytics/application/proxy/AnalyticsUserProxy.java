package com.zayenha.qatra.analytics.application.proxy;

import com.zayenha.qatra.user.api.UserApi;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyticsUserProxy {

    private final UserApi userApi;

    public UserEntity getUserReference(Long id) {
        return userApi.getUserReference(id);
    }
}
