package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra.shared.exception.ValidationException;

public class CannotDeleteActiveUserException extends ValidationException {
    public CannotDeleteActiveUserException(Long userId) {
        super("Cannot delete active user: " + userId + ". Suspend or deactivate first.", "CANNOT_DELETE_ACTIVE_USER");
        addData("userId", userId);
    }
}
