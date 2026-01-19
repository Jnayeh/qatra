package com.zayenha.qatra.user.domain.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Object id) {
        super("User not found: " + id);
    }
}
