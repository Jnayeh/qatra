package com.zayenha.qatra.user.domain.exception;

public class InvalidRoleAssignmentException extends RulesViolationException {
    public InvalidRoleAssignmentException(String message) {
        super(message);
    }
}
