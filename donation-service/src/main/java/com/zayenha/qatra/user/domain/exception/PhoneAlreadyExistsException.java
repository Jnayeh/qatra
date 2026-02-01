package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra.shared.exception.ConflictException;

public class PhoneAlreadyExistsException extends ConflictException {
    public PhoneAlreadyExistsException(String phone) {
        super("Phone already in use: " + phone, "PHONE_ALREADY_EXISTS");
        addData("phone", phone);
    }
}
