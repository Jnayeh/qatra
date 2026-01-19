package com.zayenha.qatra.user.domain.exception;

public class CannotDeleteActiveUserException extends RulesViolationException {
    public CannotDeleteActiveUserException(Long userId) {
        super("Cannot delete active user: " + userId + ". Suspend or deactivate first.");
    }
}
