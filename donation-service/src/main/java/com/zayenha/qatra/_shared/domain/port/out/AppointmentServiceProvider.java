package com.zayenha.qatra._shared.domain.port.out;

import com.zayenha.qatra._shared.domain.AppointmentType;

public interface AppointmentServiceProvider {
    long countCompletedByEmergencyId(Long emergencyId);
    void book(Long donorId, Long slotId, Long emergencyId, AppointmentType type);
}
