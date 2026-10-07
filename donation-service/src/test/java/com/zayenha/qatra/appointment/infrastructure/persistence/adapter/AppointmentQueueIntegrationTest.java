package com.zayenha.qatra.appointment.infrastructure.persistence.adapter;

import com.zayenha.qatra._shared.domain.enums.AppointmentType;
import com.zayenha.qatra._shared.domain.enums.BloodType;
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
 * The staff queue paginates by center and date range: totalElements must count
 * the rows the query actually returns (not every appointment in the database),
 * and the mapped domain objects must carry the ids and donor/slot context that
 * the API responses and service flows are built from.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentQueueIntegrationTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private AppointmentRepositoryAdapter adapter;

    @Test
    void findByCenterIdAndDateRangeCountsOnlyMatchingRowsAndEnrichesDomain() {
        var centerA = newCenter("Queue Center A", "queue-a@test.com");
        var centerB = newCenter("Queue Center B", "queue-b@test.com");
        em.persist(centerA);
        em.persist(centerB);

        var day = LocalDate.of(2026, 10, 7);
        var slotA1 = newSlot(centerA, day, LocalTime.of(9, 0));
        var slotA2 = newSlot(centerA, day, LocalTime.of(10, 0));
        var slotOtherDay = newSlot(centerA, day.plusDays(1), LocalTime.of(9, 0));
        var slotB = newSlot(centerB, day, LocalTime.of(9, 0));
        em.persist(slotA1);
        em.persist(slotA2);
        em.persist(slotOtherDay);
        em.persist(slotB);

        // The two matches: center A, queried day.
        adapter.save(new Appointment(1L, slotA1.getId(), centerA.getId(), null,
                AppointmentType.REGULAR, BloodType.O_POSITIVE));
        adapter.save(new Appointment(2L, slotA2.getId(), centerA.getId(), null,
                AppointmentType.REGULAR, null));
        // Same day but other center, and same center but other day: not counted.
        adapter.save(new Appointment(3L, slotB.getId(), centerB.getId(), null,
                AppointmentType.REGULAR, null));
        adapter.save(new Appointment(4L, slotOtherDay.getId(), centerA.getId(), null,
                AppointmentType.REGULAR, null));

        var page = adapter.findByCenterIdAndDateRange(centerA.getId(), day, day, 0, 10);

        assertThat(page.content()).hasSize(2);
        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.totalPages()).isEqualTo(1);

        var first = page.content().get(0);
        assertThat(first.getCenterId()).isEqualTo(centerA.getId());
        assertThat(first.getSlotId()).isEqualTo(slotA1.getId());
        assertThat(first.getDonorId()).isEqualTo(1L);
        assertThat(first.getSlotDate()).isEqualTo(day);
        assertThat(first.getSlotTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(first.getSlotName()).isEqualTo("09:00 - 10:00");
        assertThat(first.getDonorDisplayName()).isEqualTo("Donor One");
        assertThat(first.getDonorFirstName()).isEqualTo("Donor");
        assertThat(first.getDonorPhone()).isEqualTo("0000000001");
    }

    private CenterEntity newCenter(String name, String email) {
        var center = new CenterEntity();
        center.setName(name);
        center.setAddress("1 Test St");
        center.setCity("Testville");
        center.setCountry("TC");
        center.setPhone("000");
        center.setEmail(email);
        center.setFacilityType(FacilityType.CLINIC);
        center.setStatus(CenterStatus.ACTIVE);
        return center;
    }

    private SlotEntity newSlot(CenterEntity center, LocalDate date, LocalTime start) {
        var slot = new SlotEntity();
        slot.setCenter(center);
        slot.setDate(date);
        slot.setStartTime(start);
        slot.setEndTime(start.plusHours(1));
        slot.setMaxBookings(10);
        slot.setMaxRegularBookings(5);
        return slot;
    }
}
