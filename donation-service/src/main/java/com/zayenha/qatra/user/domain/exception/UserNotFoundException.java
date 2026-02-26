package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra._shared.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Object id) {
        super("User not found: " + id, UserErrorCode.USER_NOT_FOUND.name());
        addData("userId", id);
    }
}
