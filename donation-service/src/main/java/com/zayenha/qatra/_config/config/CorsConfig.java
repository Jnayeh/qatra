package com.zayenha.qatra._config.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Slf4j
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:http://localhost:4200,*}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Value("${cors.path-pattern:/api/**}")
    private String pathPattern;

    @Bean
    public CorsFilter corsFilter() {
        var config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of(allowedMethods.split(",")));
        config.setAllowedHeaders(List.of(allowedHeaders.split(",")));
        config.setAllowCredentials(true);
        config.setMaxAge(maxAge);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(pathPattern, config);

        // Add this to verify your pattern
        CorsConfig.log.warn("CORS configured with pattern: {}", pathPattern);
        CorsConfig.log.warn("Allowed origins: {}", allowedOrigins);

        return new CorsFilter(source);
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of(allowedMethods.split(",")));
        config.setAllowedHeaders(List.of(allowedHeaders.split(",")));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
