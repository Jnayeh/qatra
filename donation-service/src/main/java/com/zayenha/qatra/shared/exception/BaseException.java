package com.zayenha.qatra.shared.exception;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final int httpStatus;
    private final Map<String, Object> data;

    public BaseException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.data = new HashMap<>();
    }

    public String getErrorCode() { return errorCode; }
    public int getHttpStatus() { return httpStatus; }
    public Map<String, Object> getData() { return data; }

    public BaseException addData(String key, Object value) {
        data.put(key, value);
        return this;
    }
}
