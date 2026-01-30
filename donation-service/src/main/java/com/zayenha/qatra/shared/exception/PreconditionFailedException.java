package com.zayenha.qatra.shared.exception;

public class PreconditionFailedException extends BaseException {
    public PreconditionFailedException(String message) {
        super(message, "PRECONDITION_FAILED", 412);
    }

    public PreconditionFailedException(String message, String errorCode) {
        super(message, errorCode, 412);
    }
}
