package com.zayenha.qatra._shared.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Component
public class NoOpCacheService implements CacheService {

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> get(String key, TypeReference<T> typeRef) {
        return Optional.empty();
    }

    @Override
    public <T> Optional<T> getWithRefresh(String key, Class<T> type, Supplier<T> loader) {
        return Optional.ofNullable(loader.get());
    }

    @Override
    public <T> Optional<T> getWithRefresh(String key, TypeReference<T> typeRef, Supplier<T> loader) {
        return Optional.ofNullable(loader.get());
    }

    @Override
    public void put(String key, Object value) {
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
    }

    @Override
    public void evict(String key) {
    }

    @Override
    public void evictByPattern(String pattern) {
    }

    @Override
    public Set<String> keys(String pattern) {
        return Collections.emptySet();
    }

    @Override
    public boolean exists(String key) {
        return false;
    }
}
