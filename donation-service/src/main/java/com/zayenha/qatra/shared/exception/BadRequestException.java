package com.zayenha.qatra.shared.exception;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message, "BAD_REQUEST", 400);
    }

    public BadRequestException(String message, String errorCode) {
        super(message, errorCode, 400);
    }
}
