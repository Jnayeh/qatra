package com.zayenha.qatra.appointment.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.enums.AppointmentStatus;
import com.zayenha.qatra._shared.domain.enums.AppointmentType;
import com.zayenha.qatra._shared.domain.enums.CenterStatus;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.center.domain.model.FacilityType;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that booking writes the appointment with a single INSERT that already
 * contains the qr code (the qr code embeds the generated id, so it must be set
 * after the id is drawn from the sequence but before the INSERT is flushed).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentQrCodeIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AppointmentRepositoryAdapter adapter;

    @Test
    void saveAssignsIdAndPersistsQrCodeInASingleInsert() {
        var center = new CenterEntity();
        center.setName("QR Test Center");
        center.setAddress("1 Test St");
        center.setCity("Testville");
        center.setCountry("TC");
        center.setPhone("000");
        center.setEmail("qr-test@test.com");
        center.setFacilityType(FacilityType.CLINIC);
        center.setStatus(CenterStatus.ACTIVE);
        em.persist(center);

        var slot = new SlotEntity();
        slot.setCenter(center);
        slot.setDate(LocalDate.now());
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(10, 0));
        slot.setMaxBookings(10);
        slot.setMaxRegularBookings(5);
        em.persist(slot);

        var donorId = 1L; // seeded by data.sql
        var appointment = new Appointment(donorId, slot.getId(), center.getId(), null,
                AppointmentType.REGULAR, null);

        var saved = adapter.save(appointment);

        var expected = "A" + saved.getId() + "D" + donorId + "S" + slot.getId() + "C" + center.getId();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(saved.getQrCode()).isEqualTo(expected);

        em.flush();
        var persistedQr = (String) em.createNativeQuery("SELECT qr_code FROM appointments WHERE id = :id")
                .setParameter("id", saved.getId())
                .getSingleResult();
        assertThat(persistedQr).isEqualTo(expected);
    }
}
