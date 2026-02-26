package com.zayenha.qatra._shared.web;


public record Paginated(
    int number,
    int size,
    long totalElements,
    int totalPages
) {
}
