package com.zayenha.qatra.shared.exception;

public class ExternalServiceException extends BaseException {
    public ExternalServiceException(String message) {
        super(message, "EXTERNAL_SERVICE_ERROR", 502);
    }

    public ExternalServiceException(String message, String errorCode) {
        super(message, errorCode, 502);
    }
}
