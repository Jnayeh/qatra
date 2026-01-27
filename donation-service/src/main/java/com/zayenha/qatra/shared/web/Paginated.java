package com.zayenha.qatra.shared.web;


public record Paginated(
    int number,
    int size,
    long totalElements,
    int totalPages
) {
}
