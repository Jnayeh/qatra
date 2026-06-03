package com.zayenha.qatra.notification._shared.web;

public record Paginated(
    int number,
    int size,
    long totalElements,
    int totalPages
) {
}
