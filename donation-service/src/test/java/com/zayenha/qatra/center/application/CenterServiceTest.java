package com.zayenha.qatra.center.application;

import com.zayenha.qatra._shared.cache.CacheService;
import com.zayenha.qatra._shared.event.AuditPublisher;
import com.zayenha.qatra.center.domain.model.*;
import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases.CreateCenterCommand;
import com.zayenha.qatra.center.domain.port.in.CenterCommandUseCases.UpdateCenterCommand;
import com.zayenha.qatra.center.domain.port.out.CenterRepositoryPort;
import com.zayenha.qatra.center.domain.port.out.SlotRepositoryPort;
import com.zayenha.qatra._shared.domain.PageResult;
import com.zayenha.qatra._shared.domain.SearchCriteria;
import com.zayenha.qatra._shared.exception.ConflictException;
import com.zayenha.qatra._shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CenterServiceTest {

    @Mock
    private CenterRepositoryPort centerRepository;
    @Mock
    private SlotRepositoryPort slotRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private CacheService cacheService;
    @Mock
    private AuditPublisher auditPublisher;

    private CenterService centerService;

    @BeforeEach
    void setUp() {
        centerService = new CenterService(centerRepository, slotRepository, eventPublisher, cacheService, auditPublisher);
    }

    private DonationCenter aCenter() {
        var op = new OperatingHours(
            new OperatingHours.DaySchedule(LocalTime.of(8, 0), LocalTime.of(17, 0)),
            null, null, null, null, null, null, null
        );
        var center = new DonationCenter("Main Center", "123 Street", "City", "Country",
                "12345", "1234567890", "center@test.com",
                40.7128, -74.0060, FacilityType.BLOOD_BANK, op,
                100, 50, 30, 0L);
        center.setId(1L);
        return center;
    }

    private CreateCenterCommand aCreateCommand() {
        return new CreateCenterCommand("New Center", "456 Avenue", "Metropolis", "USA",
                "67890", "0987654321", "new@test.com",
                34.0522, -118.2437, FacilityType.HOSPITAL,
                new OperatingHours(null, null, null, null, null, null, null, null),
                200, 100, 45);
    }

    private UpdateCenterCommand anUpdateCommand() {
        return new UpdateCenterCommand("Updated Center", "789 Blvd", "Gotham", "USA",
                "11111", "5555555555", "updated@test.com",
                51.5074, -0.1278, FacilityType.CLINIC,
                new OperatingHours(null, null, null, null, null, null, null, null),
                150, 75, 30);
    }



    @Test
    void createSavesAndReturnsCenter() {
        when(centerRepository.existsByName("New Center")).thenReturn(false);
        when(centerRepository.save(any())).thenAnswer(invocation -> {
            var center = invocation.<DonationCenter>getArgument(0);
            center.setId(1L);
            return center;
        });

        var result = centerService.create(aCreateCommand());

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("New Center");
        assertThat(result.getStatus()).isEqualTo(CenterStatus.PENDING_APPROVAL);
        verify(centerRepository).save(any());
    }

    @Test
    void createThrowsWhenNameAlreadyExists() {
        when(centerRepository.existsByName("New Center")).thenReturn(true);

        assertThatThrownBy(() -> centerService.create(aCreateCommand()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("New Center");
        verify(centerRepository, never()).save(any());
    }



    @Test
    void updateUpdatesExistingCenter() {
        var existing = aCenter();
        when(centerRepository.otherCenterHasName(1L, "Updated Center")).thenReturn(false);
        when(centerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(centerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = centerService.update(1L, anUpdateCommand());

        assertThat(result.getName()).isEqualTo("Updated Center");
        assertThat(result.getAddress()).isEqualTo("789 Blvd");
        assertThat(result.getFacilityType()).isEqualTo(FacilityType.CLINIC);
    }

    @Test
    void updateThrowsWhenCenterNotFound() {
        when(centerRepository.otherCenterHasName(99L, "Updated Center")).thenReturn(false);
        when(centerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> centerService.update(99L, anUpdateCommand()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateThrowsWhenOtherCenterHasSameName() {
        when(centerRepository.otherCenterHasName(1L, "Updated Center")).thenReturn(true);

        assertThatThrownBy(() -> centerService.update(1L, anUpdateCommand()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Updated Center");
        verify(centerRepository, never()).save(any());
    }



    @Test
    void updateStatusChangesStatus() {
        var center = aCenter();
        when(centerRepository.findById(1L)).thenReturn(Optional.of(center));
        when(centerRepository.save(any())).thenReturn(center);

        centerService.updateStatus(1L, CenterStatus.ACTIVE);

        assertThat(center.getStatus()).isEqualTo(CenterStatus.ACTIVE);
    }

    @Test
    void updateStatusThrowsWhenCenterNotFound() {
        when(centerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> centerService.updateStatus(99L, CenterStatus.ACTIVE))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }



    @Test
    void deleteDeletesExistingCenter() {
        when(centerRepository.existsById(1L)).thenReturn(true);

        centerService.delete(1L);

        verify(centerRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenCenterNotFound() {
        when(centerRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> centerService.delete(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
        verify(centerRepository, never()).deleteById(any());
    }



    @Test
    void getByIdReturnsCenter() {
        var center = aCenter();
        when(centerRepository.findById(1L)).thenReturn(Optional.of(center));

        var result = centerService.getById(1L);

        assertThat(result).isEqualTo(center);
    }

    @Test
    void getByIdWithFetchJoinsReturnsCenter() {
        var center = aCenter();
        when(centerRepository.findById(1L, true)).thenReturn(Optional.of(center));

        var result = centerService.getById(1L, true);

        assertThat(result).isEqualTo(center);
    }

    @Test
    void getByIdThrowsWhenNotFound() {
        when(centerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> centerService.getById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }



    @Test
    void getAllDelegatesToRepository() {
        var criteria = new SearchCriteria(null, "id", "asc", 0, 20);
        var pageResult = new PageResult<DonationCenter>(List.of(aCenter()), 0, 20, 1, 1);
        when(centerRepository.findAll(criteria)).thenReturn(pageResult);

        var result = centerService.getAll(criteria);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }



    @Test
    void addStaffSavesAndReturnsStaff() {
        when(centerRepository.existsById(1L)).thenReturn(true);
        when(centerRepository.existsStaffByCenterIdAndUserId(1L, 10L)).thenReturn(false);
        when(centerRepository.saveStaff(any())).thenAnswer(i -> i.getArgument(0));

        var result = centerService.addStaff(1L, 10L);

        assertThat(result.getUserId()).isEqualTo(10L);
        assertThat(result.getCenterId()).isEqualTo(1L);
    }



    @Test
    void approveSetsActiveWhenApproved() {
        var center = aCenter();
        when(centerRepository.findById(1L)).thenReturn(Optional.of(center));
        when(centerRepository.save(any())).thenReturn(center);

        var result = centerService.approve(1L, true, "Looks good");

        assertThat(result.getStatus()).isEqualTo(CenterStatus.ACTIVE);
    }
}
