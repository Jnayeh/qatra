package com.zayenha.qatra.appointment.domain.model.vo;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ValueObjectTest {

    @Test
    void appointmentIdAcceptsNull() {
        assertThatNoException().isThrownBy(() -> new AppointmentId(null));
    }

    @Test
    void appointmentIdRejectsNegative() {
        assertThatThrownBy(() -> new AppointmentId(-1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void appointmentIdAcceptsPositive() {
        assertThatNoException().isThrownBy(() -> new AppointmentId(1L));
    }

}
