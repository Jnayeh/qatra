package com.zayenha.qatra.user.api;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserLoggedInEvent extends ApplicationEvent {
    private final Long userId;
    private final String email;

    public UserLoggedInEvent(Object source, Long userId, String email) {
        super(source);
        this.userId = userId;
        this.email = email;
    }
}
