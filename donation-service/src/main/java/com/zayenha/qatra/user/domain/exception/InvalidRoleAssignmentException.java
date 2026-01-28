package com.zayenha.qatra.user.domain.exception;

public class InvalidRoleAssignmentException extends AlreadyExistsException {
    public InvalidRoleAssignmentException(String message) {
        super(message);
    }
}
