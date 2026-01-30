package com.zayenha.qatra.shared.exception;

public class TooManyRequestsException extends BaseException {
    public TooManyRequestsException(String message) {
        super(message, "TOO_MANY_REQUESTS", 429);
    }

    public TooManyRequestsException(String message, String errorCode) {
        super(message, errorCode, 429);
    }
}
