package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra.shared.exception.ConflictException;

public class UserAlreadyExistsException extends ConflictException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
