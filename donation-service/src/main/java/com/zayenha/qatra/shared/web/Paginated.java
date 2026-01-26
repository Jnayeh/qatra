package com.zayenha.qatra.shared.web;


public record Page(
    int number,
    int size,
    long totalElements,
    int totalPages
) {
}
