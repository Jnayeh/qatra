package com.zayenha.qatra.appointment.infrastructure.mapper;

import com.zayenha.qatra._shared.domain.enums.AppointmentStatus;
import com.zayenha.qatra._shared.domain.enums.AppointmentType;
import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra.appointment.infrastructure.persistence.entity.AppointmentEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.SlotEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonorProfileEntity;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The read paths (queue, get-by-id, by-donor) go entity -> domain through
 * {@code toDomain}. Anything dropped there never reaches the API response or
 * the service layer (donor stats, slot release, center lookups).
 */
class AppointmentMapperToDomainTest {

    private final AppointmentMapper mapper = new AppointmentMapperImpl();

    @Test
    void toDomainCarriesIdsDonorAndSlot() {
        var user = new UserEntity();
        user.setFirstName("Amina");
        user.setFamilyName("Benali");
        user.setDisplayName("Amina Benali");
        user.setPhone("+216 20 000 000");
        user.setEmail("amina@example.com");

        var donor = new DonorProfileEntity();
        donor.setId(10L);
        donor.setCity("Tunis");
        donor.setUser(user);

        var slot = new SlotEntity();
        slot.setId(100L);
        slot.setDate(LocalDate.of(2026, 10, 7));
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(10, 0));

        var center = new CenterEntity();
        center.setId(1000L);

        var entity = new AppointmentEntity();
        entity.setId(1L);
        entity.setDonor(donor);
        entity.setSlot(slot);
        entity.setCenter(center);
        entity.setAppointmentType(AppointmentType.REGULAR);
        entity.setStatus(AppointmentStatus.CHECKED_IN);
        entity.setBloodType(BloodType.O_POSITIVE);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        var domain = mapper.toDomain(entity);

        assertThat(domain.getDonorId()).isEqualTo(10L);
        assertThat(domain.getSlotId()).isEqualTo(100L);
        assertThat(domain.getCenterId()).isEqualTo(1000L);
        assertThat(domain.getSlotDate()).isEqualTo(LocalDate.of(2026, 10, 7));
        assertThat(domain.getSlotTime()).isEqualTo(LocalTime.of(9, 0));
        assertThat(domain.getDonorFirstName()).isEqualTo("Amina");
        assertThat(domain.getDonorLastName()).isEqualTo("Benali");
        assertThat(domain.getDonorDisplayName()).isEqualTo("Amina Benali");
        assertThat(domain.getDonorPhone()).isEqualTo("+216 20 000 000");
        assertThat(domain.getDonorEmail()).isEqualTo("amina@example.com");
        assertThat(domain.getDonorCity()).isEqualTo("Tunis");
        assertThat(domain.getSlotName()).isEqualTo("09:00 - 10:00");
    }
}
