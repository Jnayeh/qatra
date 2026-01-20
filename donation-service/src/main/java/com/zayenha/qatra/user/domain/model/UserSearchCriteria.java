package com.zayenha.qatra.user.domain.model;

public record UserSearchCriteria(
    String search,
    String sortBy,
    String sortDirection,
    int page,
    int size
) {
    public static UserSearchCriteria defaultAll() {
        return new UserSearchCriteria(null, "id", "asc", 1, 20);
    }
}
