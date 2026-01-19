package com.zayenha.qatra.user.domain.exception;

public class RulesViolationException extends RuntimeException {
    public RulesViolationException(String message) {
        super(message);
    }
}
