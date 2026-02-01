package com.zayenha.qatra.shared.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    T data,
    Paginated page,
    String message,
    String code,
    Instant timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null, null, Instant.now());
    }
    public static <T> ApiResponse<T> success(T data, Paginated page) {
        return new ApiResponse<>(true, data, page, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, null, message, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, null, message, code, Instant.now());
    }

    public static <T> ApiResponse<T> error(T data, String message) {
        return new ApiResponse<>(false, data, null, message, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(T data, String code, String message) {
        return new ApiResponse<>(false, data, null, message, code, Instant.now());
    }
}
