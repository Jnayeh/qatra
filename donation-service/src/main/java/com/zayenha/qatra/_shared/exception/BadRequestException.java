package com.zayenha.qatra._shared.exception;

public class BadRequestException extends BaseException {
    public BadRequestException(String message, String errorCode) {
        super(message, errorCode, 400);
    }
}
