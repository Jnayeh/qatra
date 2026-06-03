package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra._shared.exception.ValidationException;

public class CannotDeleteUserException extends ValidationException {
    public CannotDeleteUserException(Long userId) {
        super("Cannot delete user: " + userId + ". Suspend or deactivate first.", UserErrorCode.CANNOT_DELETE_ACTIVE_USER.name());
        addData("userId", userId);
    }
}
