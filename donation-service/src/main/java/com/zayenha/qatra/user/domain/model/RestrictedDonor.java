package com.zayenha.qatra.user.domain.model;

public record RestrictedDonor(
    Long id,
    String email,
    String displayName,
    String status,
    Long donorId,
    boolean permanentlyRestricted,
    String restrictionReason
) {}
