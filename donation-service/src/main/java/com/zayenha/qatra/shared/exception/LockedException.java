package com.zayenha.qatra.shared.exception;

public class LockedException extends BaseException {
    public LockedException(String message) {
        super(message, "LOCKED", 423);
    }

    public LockedException(String message, String errorCode) {
        super(message, errorCode, 423);
    }
}
