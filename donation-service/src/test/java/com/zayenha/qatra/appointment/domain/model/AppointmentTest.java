package com.zayenha.qatra.appointment.domain.model;

import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class AppointmentTest {

    private static final Long DONOR_ID = 1L;
    private static final Long CENTER_ID = 10L;
    private static final Long SLOT_ID = 100L;
    private static final Long EMERGENCY_ID = 1000L;
    private static final Long STAFF_ID = 42L;

    private Appointment createScheduledAppointment() {
        return Appointment.schedule(DONOR_ID, CENTER_ID, SLOT_ID, AppointmentType.REGULAR, null);
    }

    private Appointment createScheduledEmergencyAppointment() {
        return Appointment.schedule(DONOR_ID, CENTER_ID, SLOT_ID, AppointmentType.EMERGENCY, EMERGENCY_ID);
    }

    @Nested
    class Schedule {

        @Test
        void createsAppointmentWithScheduledStatus() {
            Appointment appointment = createScheduledAppointment();
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
            assertThat(appointment.getDonorId()).isEqualTo(DONOR_ID);
            assertThat(appointment.getCenterId()).isEqualTo(CENTER_ID);
            assertThat(appointment.getSlotId()).isEqualTo(SLOT_ID);
            assertThat(appointment.getAppointmentType()).isEqualTo(AppointmentType.REGULAR);
            assertThat(appointment.getCreatedAt()).isNotNull();
        }

        @Test
        void createsEmergencyAppointmentWithEmergencyId() {
            Appointment appointment = createScheduledEmergencyAppointment();
            assertThat(appointment.getAppointmentType()).isEqualTo(AppointmentType.EMERGENCY);
            assertThat(appointment.getEmergencyId()).isEqualTo(EMERGENCY_ID);
        }

        @Test
        void regularAppointmentHasNoEmergencyId() {
            Appointment appointment = createScheduledAppointment();
            assertThat(appointment.getEmergencyId()).isNull();
        }
    }

    @Nested
    class Confirm {

        @Test
        void transitionsFromScheduledToConfirmed() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
            assertThat(appointment.getConfirmedAt()).isNotNull();
        }

        @Test
        void failsWhenNotScheduled() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            assertThatThrownBy(appointment::confirm)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("SCHEDULED");
        }

        @Test
        void failsWhenCompleted() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            appointment.complete(450, STAFF_ID);
            assertThatThrownBy(appointment::confirm)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void failsWhenCancelled() {
            Appointment appointment = createScheduledAppointment();
            appointment.cancel("Changed mind");
            assertThatThrownBy(appointment::confirm)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void failsWhenNoShow() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.markNoShow();
            assertThatThrownBy(appointment::confirm)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class Start {

        @Test
        void transitionsFromConfirmedToInProgress() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.IN_PROGRESS);
        }

        @Test
        void transitionsFromScheduledToInProgress() {
            Appointment appointment = createScheduledAppointment();
            appointment.start();
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.IN_PROGRESS);
        }

        @Test
        void failsWhenCompleted() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            appointment.complete(450, STAFF_ID);
            assertThatThrownBy(appointment::start)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void failsWhenCancelled() {
            Appointment appointment = createScheduledAppointment();
            appointment.cancel("Not coming");
            assertThatThrownBy(appointment::start)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void failsWhenNoShow() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.markNoShow();
            assertThatThrownBy(appointment::start)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class Complete {

        @Test
        void transitionsFromInProgressToCompleted() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            appointment.complete(450, STAFF_ID);
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
            assertThat(appointment.getMlCollected()).isEqualTo(450);
            assertThat(appointment.getCompletedByStaffId()).isEqualTo(STAFF_ID);
            assertThat(appointment.getCompletedAt()).isNotNull();
        }

        @Test
        void failsWhenMlCollectedIsNull() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            assertThatThrownBy(() -> appointment.complete(null, STAFF_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void failsWhenMlCollectedIsZero() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            assertThatThrownBy(() -> appointment.complete(0, STAFF_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void failsWhenMlCollectedIsNegative() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            assertThatThrownBy(() -> appointment.complete(-100, STAFF_ID))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void failsWhenStaffIdIsNull() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            assertThatThrownBy(() -> appointment.complete(450, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void failsWhenNotInProgress() {
            Appointment appointment = createScheduledAppointment();
            assertThatThrownBy(() -> appointment.complete(450, STAFF_ID))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("IN_PROGRESS");
        }

        @Test
        void failsWhenAlreadyCompleted() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            appointment.complete(450, STAFF_ID);
            assertThatThrownBy(() -> appointment.complete(500, STAFF_ID))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void failsWhenCancelled() {
            Appointment appointment = createScheduledAppointment();
            appointment.cancel("Not coming");
            assertThatThrownBy(() -> appointment.complete(450, STAFF_ID))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class Cancel {

        @Test
        void cancelsScheduledAppointment() {
            Appointment appointment = createScheduledAppointment();
            appointment.cancel("Personal reasons");
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
            assertThat(appointment.getCancellationReason()).isEqualTo("Personal reasons");
            assertThat(appointment.getCancelledAt()).isNotNull();
        }

        @Test
        void cancelsConfirmedAppointment() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.cancel("Emergency at work");
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        }

        @Test
        void cancelsInProgressAppointment() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            appointment.cancel("Donor feeling unwell");
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
        }

        @Test
        void failsWhenAlreadyCompleted() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            appointment.complete(450, STAFF_ID);
            assertThatThrownBy(() -> appointment.cancel("Too late"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        void failsWhenAlreadyCancelled() {
            Appointment appointment = createScheduledAppointment();
            appointment.cancel("First reason");
            assertThatThrownBy(() -> appointment.cancel("Second reason"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("CANCELLED");
        }
    }

    @Nested
    class MarkNoShow {

        @Test
        void marksScheduledAsNoShow() {
            Appointment appointment = createScheduledAppointment();
            appointment.markNoShow();
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
        }

        @Test
        void marksConfirmedAsNoShow() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.markNoShow();
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
        }

        @Test
        void failsWhenInProgress() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            assertThatThrownBy(appointment::markNoShow)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void failsWhenCompleted() {
            Appointment appointment = createScheduledAppointment();
            appointment.confirm();
            appointment.start();
            appointment.complete(450, STAFF_ID);
            assertThatThrownBy(appointment::markNoShow)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void failsWhenAlreadyCancelled() {
            Appointment appointment = createScheduledAppointment();
            appointment.cancel("Not coming");
            assertThatThrownBy(appointment::markNoShow)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void failsWhenAlreadyNoShow() {
            Appointment appointment = createScheduledAppointment();
            appointment.markNoShow();
            assertThatThrownBy(appointment::markNoShow)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class Reconstruct {

        @Test
        void restoresScheduledAppointment() {
            Instant now = Instant.now();
            Appointment appointment = Appointment.reconstruct(
                    new AppointmentId(1L), DONOR_ID, CENTER_ID, SLOT_ID,
                    AppointmentType.REGULAR, null, AppointmentStatus.SCHEDULED,
                    null, null, null, null, null,
                    now, null, null, null
            );
            assertThat(appointment.getId().value()).isEqualTo(1L);
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
            assertThat(appointment.getCreatedAt()).isEqualTo(now);
        }

        @Test
        void restoresCompletedAppointment() {
            Instant now = Instant.now();
            Appointment appointment = Appointment.reconstruct(
                    new AppointmentId(1L), DONOR_ID, CENTER_ID, SLOT_ID,
                    AppointmentType.REGULAR, null, AppointmentStatus.COMPLETED,
                    450, "Good donation", null, null, STAFF_ID,
                    now, now, now, null
            );
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.COMPLETED);
            assertThat(appointment.getMlCollected()).isEqualTo(450);
            assertThat(appointment.getCompletedByStaffId()).isEqualTo(STAFF_ID);
        }

        @Test
        void restoresCancelledAppointment() {
            Instant now = Instant.now();
            Appointment appointment = Appointment.reconstruct(
                    new AppointmentId(1L), DONOR_ID, CENTER_ID, SLOT_ID,
                    AppointmentType.REGULAR, null, AppointmentStatus.CANCELLED,
                    null, null, "Changed mind", null, null,
                    now, null, null, now
            );
            assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
            assertThat(appointment.getCancellationReason()).isEqualTo("Changed mind");
        }

        @Test
        void restoresEmergencyAppointment() {
            Instant now = Instant.now();
            Appointment appointment = Appointment.reconstruct(
                    new AppointmentId(1L), DONOR_ID, CENTER_ID, SLOT_ID,
                    AppointmentType.EMERGENCY, EMERGENCY_ID, AppointmentStatus.SCHEDULED,
                    null, null, null, null, null,
                    now, null, null, null
            );
            assertThat(appointment.getAppointmentType()).isEqualTo(AppointmentType.EMERGENCY);
            assertThat(appointment.getEmergencyId()).isEqualTo(EMERGENCY_ID);
        }
    }
}
