package com.zayenha.qatra._shared.domain;

public record SearchCriteria(
    String search,
    String sortBy,
    String sortDirection,
    int page,
    int size
) {
    public static SearchCriteria defaultAll() {
        return new SearchCriteria(null, "id", "asc", 1, 20);
    }
}
