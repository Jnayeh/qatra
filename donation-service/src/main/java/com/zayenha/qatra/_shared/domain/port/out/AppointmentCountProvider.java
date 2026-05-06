package com.zayenha.qatra._shared.domain.port.out;

public interface AppointmentCountProvider {
    long countCompletedByEmergencyId(Long emergencyId);
}
