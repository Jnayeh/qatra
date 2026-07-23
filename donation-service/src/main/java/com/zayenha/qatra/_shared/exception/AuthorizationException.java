package com.zayenha.qatra._shared.exception;

public class AuthorizationException extends BaseException {
    public AuthorizationException(String message, String errorCode) {
        super(message, errorCode, 403);
    }
}
