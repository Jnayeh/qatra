package com.zayenha.qatra.donor.domain.service;

import com.zayenha.qatra.donor.domain.exception.DonorErrorCode;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.shared.domain.BloodType;
import com.zayenha.qatra.shared.exception.ConflictException;
import com.zayenha.qatra.shared.exception.NotFoundException;
import com.zayenha.qatra.shared.exception.ValidationException;

public class DonorDomainValidator {

    public void validateBloodTypeUpdate(DonorProfile profile) {
        if (profile.isBloodTypeVerified()) {
            throw new ConflictException("Blood type already verified and cannot be changed",
                    DonorErrorCode.BLOOD_TYPE_ALREADY_VERIFIED.name());
        }
    }

    public void validateBloodType(BloodType bloodType) {
        if (bloodType == null) {
            throw new ValidationException("Blood type is required",
                    "BLOOD_TYPE_REQUIRED");
        }
    }

    public void ensureProfileExists(DonorProfile profile, Long userId) {
        if (profile == null) {
            throw new NotFoundException("Donor profile not found for user: " + userId,
                    DonorErrorCode.DONOR_NOT_FOUND.name());
        }
    }

    public void ensureDonorExists(DonorProfile profile, Long donorId) {
        if (profile == null) {
            throw new NotFoundException("Donor not found: " + donorId,
                    DonorErrorCode.DONOR_NOT_FOUND.name());
        }
    }
}
