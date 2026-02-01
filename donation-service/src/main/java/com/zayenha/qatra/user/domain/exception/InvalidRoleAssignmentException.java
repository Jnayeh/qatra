package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra.shared.exception.ValidationException;

public class InvalidRoleAssignmentException extends ValidationException {
    public InvalidRoleAssignmentException(String message) {
        super(message, "INVALID_ROLE_ASSIGNMENT");
    }
}
