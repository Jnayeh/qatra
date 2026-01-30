package com.zayenha.qatra.shared.exception;

public class DataIntegrityException extends BaseException {
    public DataIntegrityException(String message) {
        super(message, "DATA_INTEGRITY_ERROR", 409);
    }

    public DataIntegrityException(String message, String errorCode, int httpStatus) {
        super(message, errorCode, httpStatus);
    }
}
