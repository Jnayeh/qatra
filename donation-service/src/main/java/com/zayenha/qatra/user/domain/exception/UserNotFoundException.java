package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra.shared.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Object id) {
        super("User not found: " + id, "USER_NOT_FOUND");
        addData("userId", id);
    }
}
