package com.zayenha.qatra._shared.exception;

public class NotFoundException extends BaseException {
    public NotFoundException(String message, String errorCode) {
        super(message, errorCode, 404);
    }
}
