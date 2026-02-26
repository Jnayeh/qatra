package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra._shared.exception.ValidationException;

public class InvalidRoleAssignmentException extends ValidationException {
    public InvalidRoleAssignmentException(String message) {
        super(message, UserErrorCode.INVALID_ROLE_ASSIGNMENT.name());
    }
}
