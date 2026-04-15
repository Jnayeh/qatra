package com.zayenha.qatra._shared.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

@Component
@Primary
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
public class RedisCacheService implements CacheService {

    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${cache.refresh-ahead.enabled:false}")
    private boolean refreshAheadEnabled;

    @Value("${cache.refresh-ahead.trigger-threshold-seconds:60}")
    private long refreshTriggerThresholdSeconds;

    @Value("${cache.refresh-ahead.refresh-ttl-seconds:300}")
    private long refreshTtlSeconds;

    private final ConcurrentHashMap<String, AtomicBoolean> refreshLocks = new ConcurrentHashMap<>();

    public RedisCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        var raw = redisTemplate.opsForValue().get(key);
        if (raw == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(raw, type));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> get(String key, TypeReference<T> typeRef) {
        var raw = redisTemplate.opsForValue().get(key);
        if (raw == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(raw, typeRef));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> getWithRefresh(String key, Class<T> type, Supplier<T> loader) {
        var raw = redisTemplate.opsForValue().get(key);
        if (raw != null) {
            try {
                T value = objectMapper.readValue(raw, type);
                if (refreshAheadEnabled) {
                    tryRefresh(key, loader);
                }
                return Optional.of(value);
            } catch (JsonProcessingException e) {
                return Optional.empty();
            }
        }
        return loadAndCache(key, loader);
    }

    @Override
    public <T> Optional<T> getWithRefresh(String key, TypeReference<T> typeRef, Supplier<T> loader) {
        var raw = redisTemplate.opsForValue().get(key);
        if (raw != null) {
            try {
                T value = objectMapper.readValue(raw, typeRef);
                if (refreshAheadEnabled) {
                    tryRefresh(key, loader);
                }
                return Optional.of(value);
            } catch (JsonProcessingException e) {
                return Optional.empty();
            }
        }
        return loadAndCache(key, loader);
    }

    @Override
    public void put(String key, Object value) {
        try {
            var json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cache value", e);
        }
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        try {
            var json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl.toSeconds(), TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize cache value", e);
        }
    }

    @Override
    public void evict(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void evictByPattern(String pattern) {
        var matched = redisTemplate.keys(pattern);
        if (matched != null && !matched.isEmpty()) {
            redisTemplate.delete(matched);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        var matched = redisTemplate.keys(pattern);
        return matched != null ? matched : Collections.emptySet();
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    private <T> Optional<T> loadAndCache(String key, Supplier<T> loader) {
        T loaded = loader.get();
        if (loaded != null) {
            put(key, loaded, Duration.ofSeconds(refreshTtlSeconds));
        }
        return Optional.ofNullable(loaded);
    }

    private <T> void tryRefresh(String key, Supplier<T> loader) {
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        if (ttl == null || ttl <= 0 || ttl > refreshTriggerThresholdSeconds) return;

        var lock = refreshLocks.computeIfAbsent(key, k -> new AtomicBoolean(false));
        if (!lock.compareAndSet(false, true)) return;

        CompletableFuture.runAsync(() -> {
            try {
                T newValue = loader.get();
                if (newValue != null) {
                    put(key, newValue, Duration.ofSeconds(refreshTtlSeconds));
                    log.debug("Refresh-ahead refreshed key={}", key);
                }
            } catch (Exception e) {
                log.warn("Refresh-ahead failed for key={}", key, e);
            } finally {
                refreshLocks.remove(key);
            }
        });
    }
}
