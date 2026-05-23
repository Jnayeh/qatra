package com.zayenha.qatra._shared.domain;

public record SearchCriteria(
    String search,
    String sortBy,
    String sortDirection,
    int page,
    int size
) {}
