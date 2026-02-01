package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra.shared.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email, "EMAIL_ALREADY_EXISTS");
        addData("email", email);
    }
}
