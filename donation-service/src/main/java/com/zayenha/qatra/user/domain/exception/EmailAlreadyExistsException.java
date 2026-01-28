package com.zayenha.qatra.user.domain.exception;

public class EmailAlreadyExistsException extends AlreadyExistsException {
    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email);
    }
}
