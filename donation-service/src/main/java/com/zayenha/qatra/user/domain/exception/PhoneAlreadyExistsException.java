package com.zayenha.qatra.user.domain.exception;

import com.zayenha.qatra._shared.exception.ConflictException;

public class PhoneAlreadyExistsException extends ConflictException {
    public PhoneAlreadyExistsException(String phone) {
        super("Phone already in use: " + phone, UserErrorCode.PHONE_ALREADY_EXISTS.name());
        addData("phone", phone);
    }
}
