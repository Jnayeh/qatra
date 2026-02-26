package com.zayenha.qatra.donor.domain.port.in;

import com.zayenha.qatra.donor.domain.model.AvailabilityStatus;
import com.zayenha.qatra.donor.domain.model.DonorProfile;
import com.zayenha.qatra.donor.domain.model.NotificationPreferences;
import com.zayenha.qatra._shared.domain.BloodType;

public interface DonorCommandUseCases {
    DonorProfile updateProfile(Long userId, UpdateProfileCommand command);
    DonorProfile updateBloodType(Long userId, BloodType bloodType);
    DonorProfile updateBloodTypeAdmin(Long donorId, BloodType bloodType);
    DonorProfile updateLocation(Long userId, UpdateLocationCommand command);
    DonorProfile updateAvailability(Long userId, AvailabilityStatus status);
    DonorProfile updateNotificationPrefs(Long userId, NotificationPreferences prefs);
    void         requestDeletion(Long userId);
    DonorProfile updateRestriction(Long donorId, boolean permanentlyRestricted, String reason);
    DonorProfile updateFlag(Long donorId, boolean flagged);

    record UpdateProfileCommand(String displayName, String phone) {}
    record UpdateLocationCommand(Double latitude, Double longitude, String city, String country) {}
}
