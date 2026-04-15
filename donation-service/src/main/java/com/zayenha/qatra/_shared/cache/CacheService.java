package com.zayenha.qatra._shared.cache;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public interface CacheService {

    <T> Optional<T> get(String key, Class<T> type);

    <T> Optional<T> get(String key, TypeReference<T> typeRef);

    <T> Optional<T> getWithRefresh(String key, Class<T> type, Supplier<T> loader);

    <T> Optional<T> getWithRefresh(String key, TypeReference<T> typeRef, Supplier<T> loader);

    void put(String key, Object value);

    void put(String key, Object value, Duration ttl);

    void evict(String key);

    void evictByPattern(String pattern);

    Set<String> keys(String pattern);

    boolean exists(String key);
}
