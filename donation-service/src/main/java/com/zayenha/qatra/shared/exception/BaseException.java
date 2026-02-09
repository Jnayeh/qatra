package com.zayenha.qatra.shared.exception;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class BaseException extends RuntimeException {
    private final String errorCode;
    private final int httpStatus;
    private Map<String, Object> data;

    public BaseException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public void addData(String key, Object value) {
        if (data == null) {
            data = new HashMap<>();
        }
        data.put(key, value);
    }
}
