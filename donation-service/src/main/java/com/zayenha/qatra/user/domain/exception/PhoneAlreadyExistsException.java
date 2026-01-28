package com.zayenha.qatra.user.domain.exception;

public class PhoneAlreadyExistsException extends AlreadyExistsException {
    public PhoneAlreadyExistsException(String phone) {
        super("Phone already in use: " + phone);
    }
}
