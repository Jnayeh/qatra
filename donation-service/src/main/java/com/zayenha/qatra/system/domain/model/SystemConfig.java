package com.zayenha.qatra.system.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class SystemConfig {
    private Long id;
    private String configKey;
    private String configValue;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public SystemConfig() {}

    public SystemConfig(String configKey, String configValue, String description) {
        this.configKey = configKey;
        this.configValue = configValue;
        this.description = description;
    }
}
