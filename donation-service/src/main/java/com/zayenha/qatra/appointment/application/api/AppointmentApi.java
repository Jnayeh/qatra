package com.zayenha.qatra.appointment.application.api;

import com.zayenha.qatra._shared.domain.port.out.AppointmentServiceProvider;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra._shared.domain.AppointmentType;
import com.zayenha.qatra.appointment.domain.port.in.AppointmentCommandUseCases;
import com.zayenha.qatra.appointment.domain.port.out.AppointmentRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AppointmentApi implements AppointmentServiceProvider {

    private final AppointmentRepositoryPort appointmentRepositoryPort;
    private final AppointmentCommandUseCases appointmentCommand;

    public List<Appointment> findByEmergencyId(Long emergencyId) {
        return appointmentRepositoryPort.findByEmergencyId(emergencyId);
    }
    public void book(Long donorId, Long slotId, Long emergencyId, AppointmentType type) {
        appointmentCommand.book(donorId, slotId, emergencyId, type);
    }

    @Override
    public long countCompletedByEmergencyId(Long emergencyId) {
        return appointmentRepositoryPort.countCompletedByEmergencyId(emergencyId);
    }
}
