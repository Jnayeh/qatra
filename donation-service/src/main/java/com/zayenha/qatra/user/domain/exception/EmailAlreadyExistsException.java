package com.zayenha.qatra.user.domain.exception;

public class EmailAlreadyExistsException extends UserAlreadyExistsException {
    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email);
    }
}
