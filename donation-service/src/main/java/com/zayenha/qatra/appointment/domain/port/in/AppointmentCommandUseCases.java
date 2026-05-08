package com.zayenha.qatra.appointment.domain.port.in;

import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra._shared.domain.AppointmentType;
import com.zayenha.qatra.appointment.domain.model.DonationOutcome;
import com.zayenha.qatra.appointment.domain.model.HealthScreening;

public interface AppointmentCommandUseCases {
    Appointment book(Long donorId, Long slotId, Long emergencyId, AppointmentType type);
    Appointment checkIn(Long appointmentId);
    Appointment startScreening(Long appointmentId);
    Appointment complete(Long appointmentId, DonationOutcome outcome, String notes);
    Appointment cancel(Long appointmentId);
    Appointment cancelByDonor(Long appointmentId, Long donorId);
    Appointment markNoShow(Long appointmentId);
    HealthScreening saveScreening(Long appointmentId, double weight, String bloodPressure,
                                   double hemoglobin, double temperature, boolean eligible, String notes);
}
