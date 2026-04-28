package com.zayenha.qatra._config.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Configuration
    @ConditionalOnProperty(value = "spring.cache.type", havingValue = "redis")
    static class RedisCacheConfig {

        @Bean
        public RedisConnectionFactory redisConnectionFactory(
                @Value("${spring.data.redis.host:localhost}") String host,
                @Value("${spring.data.redis.port:6379}") int port) {
            return new LettuceConnectionFactory(host, port);
        }

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory factory, Environment env) {
            var objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            objectMapper.activateDefaultTyping(
                    objectMapper.getPolymorphicTypeValidator(),
                    ObjectMapper.DefaultTyping.EVERYTHING);

            var serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
            var keySerializer = new StringRedisSerializer();

            var baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                    .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer))
                    .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                    .entryTtl(Duration.ofSeconds(300));

            var builder = RedisCacheManager.builder(factory)
                    .cacheDefaults(baseConfig);

            Map<String, Long> ttls = Map.ofEntries(
                    Map.entry("users", ttl(env, "user.entity-user.ttl", 3600L)),
                    Map.entry("userExists", ttl(env, "user.entity-user.ttl", 3600L)),
                    Map.entry("userRoles", ttl(env, "user.entity-user.ttl", 3600L)),
                    Map.entry("donorProfiles", ttl(env, "donor.entity-donorprofile.ttl", 3600L)),
                    Map.entry("impactResults", ttl(env, "donor.entity-impact.ttl", 300L)),
                    Map.entry("donationCenters", ttl(env, "center.entity-donationcenter.ttl", 1800L)),
                    Map.entry("slots", ttl(env, "center.entity-slot.ttl", 120L)),
                    Map.entry("centerStaff", ttl(env, "center.entity-staff.ttl", 3600L)),
                    Map.entry("appointments", ttl(env, "appointment.entity-appointment.ttl", 120L)),
                    Map.entry("screenings", ttl(env, "appointment.entity-screening.ttl", 120L)),
                    Map.entry("emergencies", ttl(env, "emergency.entity-emergency.ttl", 60L)),
                    Map.entry("responses", ttl(env, "emergency.entity-response.ttl", 120L))
            );

            ttls.forEach((name, ttlValue) ->
                    builder.withCacheConfiguration(name, baseConfig.entryTtl(Duration.ofSeconds(ttlValue))));

            return builder.build();
        }
    }

    @Configuration
    @ConditionalOnMissingBean(CacheManager.class)
    static class SimpleFallbackConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }

    private static long ttl(Environment env, String key, long fallback) {
        Long val = env.getProperty(key, Long.class);
        return val != null ? val : fallback;
    }
}
