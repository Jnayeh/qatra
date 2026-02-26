package com.zayenha.qatra._shared.exception;

public class ValidationException extends BaseException {
    public ValidationException(String message, String errorCode) {
        super(message, errorCode, 422);
    }
}
