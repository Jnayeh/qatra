package com.zayenha.qatra.center.infrastructure.web.mapper;

import com.zayenha.qatra.center.domain.model.CenterStatus;
import com.zayenha.qatra.center.domain.model.DonationCenter;
import com.zayenha.qatra.center.domain.model.FacilityType;
import com.zayenha.qatra.center.domain.model.OperatingHours;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class CenterMapperTest {

    private DonationCenter aCenter() {
        var op = new OperatingHours(
            new OperatingHours.DaySchedule(LocalTime.of(8, 0), LocalTime.of(17, 0)),
            null, null, null, null, null, null, null
        );
        var center = new DonationCenter("Main Center", "123 Street", "City", "Country",
                "12345", "1234567890", "center@test.com",
                40.7128, -74.0060, FacilityType.BLOOD_BANK, op,
                100, 50, 30);
        center.setId(1L);
        center.setStatus(CenterStatus.ACTIVE);
        center.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
        center.setUpdatedAt(Instant.parse("2025-06-01T00:00:00Z"));
        return center;
    }

    @Test
    void toResponseMapsAllFields() {
        var center = aCenter();
        var response = CenterMapper.toResponse(center);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Main Center");
        assertThat(response.address()).isEqualTo("123 Street");
        assertThat(response.city()).isEqualTo("City");
        assertThat(response.country()).isEqualTo("Country");
        assertThat(response.postalCode()).isEqualTo("12345");
        assertThat(response.phone()).isEqualTo("1234567890");
        assertThat(response.email()).isEqualTo("center@test.com");
        assertThat(response.latitude()).isEqualTo(40.7128);
        assertThat(response.longitude()).isEqualTo(-74.0060);
        assertThat(response.facilityType()).isEqualTo(FacilityType.BLOOD_BANK);
        assertThat(response.operatingHours()).isNotNull();
        assertThat(response.operatingHours().monday()).isNotNull();
        assertThat(response.operatingHours().monday().open()).isEqualTo(LocalTime.of(8, 0));
        assertThat(response.status()).isEqualTo(CenterStatus.ACTIVE);
        assertThat(response.totalCapacity()).isEqualTo(100);
        assertThat(response.maxRegular()).isEqualTo(50);
        assertThat(response.slotPeriod()).isEqualTo(30);
        assertThat(response.createdAt()).isEqualTo("2025-01-01T00:00:00Z");
        assertThat(response.updatedAt()).isEqualTo("2025-06-01T00:00:00Z");
    }
}
