package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra._shared.domain.BloodType;
import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import jakarta.validation.constraints.NotNull;

public interface DonorCommandUseCases {
    DonorProfile updateProfile(Long userId);
    DonorProfile updateBloodType(Long userId, BloodType bloodType);
    DonorProfile updateBloodTypeAdmin(Long donorId, BloodType bloodType);
    DonorProfile updateLocation(Long userId, UpdateLocationCommand command);
    DonorProfile updateAvailability(Long userId, AvailabilityStatus status);
    DonorProfile updateNotificationPrefs(Long userId, NotificationPreferences prefs);
    void         requestDeletion(Long userId);
    void         reactivateDonor(Long userId);
    DonorProfile updateRestriction(Long donorId, boolean permanentlyRestricted, String reason);
    DonorProfile updateFlag(Long donorId, boolean flagged);
    void activateProfile(Long userId);

    record UpdateLocationCommand(@NotNull Double latitude, @NotNull Double longitude, String city, String country) {}
}
