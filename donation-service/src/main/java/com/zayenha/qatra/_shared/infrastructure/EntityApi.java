package com.zayenha.qatra._shared.infrastructure;

public interface EntityApi<T> {
    T getReference(Long id);
}
