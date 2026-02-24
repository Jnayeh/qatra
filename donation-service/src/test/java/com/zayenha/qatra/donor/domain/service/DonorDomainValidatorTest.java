package com.zayenha.qatra.donor.domain.service;

import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.shared.domain.BloodType;
import com.zayenha.qatra.shared.exception.ConflictException;
import com.zayenha.qatra.shared.exception.NotFoundException;
import com.zayenha.qatra.shared.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DonorDomainValidatorTest {

    private final DonorDomainValidator validator = new DonorDomainValidator();

    @Test
    void validateBloodTypeUpdatePassesWhenNotVerified() {
        var profile = new DonorProfile(1L);
        assertThatCode(() -> validator.validateBloodTypeUpdate(profile))
                .doesNotThrowAnyException();
    }

    @Test
    void validateBloodTypeUpdateThrowsWhenVerified() {
        var profile = new DonorProfile(1L);
        profile.setBloodType(BloodType.A_POSITIVE);
        profile.setBloodTypeVerified(true);

        assertThatThrownBy(() -> validator.validateBloodTypeUpdate(profile))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already verified");
    }

    @Test
    void validateBloodTypePassesWhenNotNull() {
        assertThatCode(() -> validator.validateBloodType(BloodType.A_POSITIVE))
                .doesNotThrowAnyException();
    }

    @Test
    void validateBloodTypeThrowsWhenNull() {
        assertThatThrownBy(() -> validator.validateBloodType(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Blood type is required");
    }

    @Test
    void ensureProfileExistsThrowsWhenNull() {
        assertThatThrownBy(() -> validator.ensureProfileExists(null, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Donor profile not found");
    }

    @Test
    void ensureProfileExistsPassesWhenNotNull() {
        var profile = new DonorProfile(1L);
        assertThatCode(() -> validator.ensureProfileExists(profile, 1L))
                .doesNotThrowAnyException();
    }
}
