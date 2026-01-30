package com.zayenha.qatra.shared.exception;

public class NotImplementedException extends BaseException {
    public NotImplementedException(String message) {
        super(message, "NOT_IMPLEMENTED", 501);
    }

    public NotImplementedException(String message, String errorCode) {
        super(message, errorCode, 501);
    }
}
