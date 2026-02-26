package com.zayenha.qatra._shared.exception;

public class ConflictException extends BaseException {
    public ConflictException(String message, String errorCode) {
        super(message, errorCode, 409);
    }
}
