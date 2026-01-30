package com.zayenha.qatra.shared.exception;

public class NotFoundException extends BaseException {
    public NotFoundException(String message, String errorCode) {
        super(message, errorCode, 404);
    }
}
