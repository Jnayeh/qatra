package com.zayenha.qatra.shared.exception;

public class ConflictException extends BaseException {
    public ConflictException(String message, String errorCode) {
        super(message, errorCode, 409);
    }
}
