package com.zayenha.qatra._shared.cache;

import com.fasterxml.jackson.core.type.TypeReference;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {

    <T> Optional<T> get(String key, Class<T> type);

    <T> Optional<T> get(String key, TypeReference<T> typeRef);

    void put(String key, Object value);

    void put(String key, Object value, Duration ttl);

    void evict(String key);

    void evictByPattern(String pattern);
}
