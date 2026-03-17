package com.zayenha.qatra.system.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class FeatureFlag {
    private Long id;
    private String flagName;
    private boolean enabled;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public FeatureFlag() {}

    public FeatureFlag(String flagName, boolean enabled, String description) {
        this.flagName = flagName;
        this.enabled = enabled;
        this.description = description;
    }

    public void enable() { this.enabled = true; }
    public void disable() { this.enabled = false; }
}
