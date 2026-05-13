package com.zayenha.qatra.user.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank @Email String email,
    String phone,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String firstName,
    @NotBlank String familyName,
    String displayName
) {}
