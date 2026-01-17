package com.zayenha.qatra.appointment.adapter.in.web;

import com.zayenha.qatra.appointment.adapter.in.web.dto.*;
import com.zayenha.qatra.appointment.application.port.in.*;
import com.zayenha.qatra.appointment.domain.model.Appointment;
import com.zayenha.qatra.appointment.domain.model.vo.AppointmentId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final ScheduleAppointmentUseCase scheduleAppointmentUseCase;
    private final RescheduleAppointmentUseCase rescheduleAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;
    private final CheckInAppointmentUseCase checkInAppointmentUseCase;
    private final CompleteAppointmentUseCase completeAppointmentUseCase;
    private final MarkNoShowUseCase markNoShowUseCase;
    private final ViewAppointmentsUseCase viewAppointmentsUseCase;
    private final AdminDashboardUseCase adminDashboardUseCase;

    @PostMapping
    public ResponseEntity<AppointmentResponse> schedule(@Valid @RequestBody ScheduleAppointmentRequest request) {
        var command = new ScheduleAppointmentUseCase.ScheduleAppointmentCommand(
                request.donorId(),
                request.centerId(),
                request.slotId(),
                request.appointmentType(),
                request.emergencyId()
        );
        Appointment appointment = scheduleAppointmentUseCase.schedule(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.from(appointment));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Void> reschedule(@PathVariable Long id,
                                           @Valid @RequestBody RescheduleAppointmentRequest request) {
        rescheduleAppointmentUseCase.reschedule(
                new RescheduleAppointmentUseCase.RescheduleAppointmentCommand(
                        new AppointmentId(id),
                        request.newSlotId()
                )
        );
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateStatus(@PathVariable Long id,
                                              @Valid @RequestBody UpdateAppointmentStatusRequest request) {
        var appointmentId = new AppointmentId(id);
        switch (request.status()) {
            case IN_PROGRESS:
                checkInAppointmentUseCase.checkIn(
                        new CheckInAppointmentUseCase.CheckInAppointmentCommand(appointmentId)
                );
                break;
            case COMPLETED:
                completeAppointmentUseCase.complete(
                        new CompleteAppointmentUseCase.CompleteAppointmentCommand(
                                appointmentId,
                                request.mlCollected(),
                                request.completedByStaffId()
                        )
                );
                break;
            case CANCELLED:
                cancelAppointmentUseCase.cancel(
                        new CancelAppointmentUseCase.CancelAppointmentCommand(
                                appointmentId,
                                request.reason()
                        )
                );
                break;
            case NO_SHOW:
                markNoShowUseCase.markNoShow(
                        new MarkNoShowUseCase.MarkNoShowCommand(appointmentId)
                );
                break;
            default:
                return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments(
            @RequestParam(required = false) Long centerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var query = new ViewAppointmentsUseCase.AppointmentsQuery(
                centerId, from, to, pageable.getPageNumber(), pageable.getPageSize(),
                pageable.getSort().stream().findFirst().map(Sort.Order::getProperty).orElse("createdAt"),
                pageable.getSort().stream().findFirst().map(o -> o.getDirection().name().toLowerCase()).orElse("desc")
        );
        var result = viewAppointmentsUseCase.getAllAppointments(query);
        List<AppointmentResponse> content = result.appointments().stream().map(AppointmentResponse::from).toList();
        return ResponseEntity.ok(new PageImpl<>(content, pageable, result.totalElements()));
    }

    @GetMapping("/donor/{donorId}")
    public ResponseEntity<List<AppointmentResponse>> getDonorHistory(@PathVariable Long donorId) {
        List<Appointment> appointments = viewAppointmentsUseCase.getDonorHistory(donorId);
        return ResponseEntity.ok(appointments.stream().map(AppointmentResponse::from).toList());
    }

    @GetMapping("/center/{centerId}/daily")
    public ResponseEntity<List<AppointmentResponse>> getDailySchedule(
            @PathVariable Long centerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Appointment> appointments = viewAppointmentsUseCase.getDailySchedule(centerId, date);
        return ResponseEntity.ok(appointments.stream().map(AppointmentResponse::from).toList());
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from == null) from = LocalDate.now().minusDays(30);
        if (to == null) to = LocalDate.now();
        var stats = adminDashboardUseCase.getStats();
        var byCenter = adminDashboardUseCase.getDonationSummaryByCenter();
        var dailyStats = adminDashboardUseCase.getDailyDonationStats(from, to);
        return ResponseEntity.ok(DashboardResponse.from(stats, byCenter, dailyStats));
    }
}
