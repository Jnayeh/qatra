package com.zayenha.qatra.appointment.application.api;

import com.zayenha.qatra._shared.domain.port.out.AppointmentCountProvider;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AppointmentApi implements AppointmentCountProvider {

    private final AppointmentRepositoryPort appointmentRepositoryPort;

    public List<Appointment> findByEmergencyId(Long emergencyId) {
        return appointmentRepositoryPort.findByEmergencyId(emergencyId);
    }

    @Override
    public long countCompletedByEmergencyId(Long emergencyId) {
        return appointmentRepositoryPort.countCompletedByEmergencyId(emergencyId);
    }
}
