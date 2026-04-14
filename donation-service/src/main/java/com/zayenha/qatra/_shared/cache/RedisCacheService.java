package com.zayenha.qatra._shared.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Primary
@ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
public class RedisCacheService implements CacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

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
}
