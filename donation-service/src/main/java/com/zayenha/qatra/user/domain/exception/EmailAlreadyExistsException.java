package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra._shared.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("Email already in use: " + email, UserErrorCode.EMAIL_ALREADY_EXISTS.name());
        addData("email", email);
    }
}
