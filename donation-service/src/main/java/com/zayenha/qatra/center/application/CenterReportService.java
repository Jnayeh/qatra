package com.zayenha.qatra.center.application;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CenterReportService {

    @PersistenceContext
    private EntityManager em;

    public String generateCsvReport(Long centerId, LocalDate startDate, LocalDate endDate) {
        var sb = new StringBuilder();
        sb.append("Center Report,").append(centerId).append("\n");
        sb.append("Period,").append(startDate).append(" to ").append(endDate).append("\n\n");

        appendAppointmentSection(sb, centerId, startDate, endDate);
        sb.append("\n");
        appendEmergencySection(sb, centerId, startDate, endDate);
        sb.append("\n");
        appendDonorResponseSection(sb, centerId, startDate, endDate);

        return sb.toString();
    }

    private void appendAppointmentSection(StringBuilder sb, Long centerId, LocalDate startDate, LocalDate endDate) {
        sb.append("=== APPOINTMENTS ===\n");
        sb.append("Metric,Value\n");

        var total = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM appointments WHERE center_id = :cid AND created_at >= :start AND created_at < :end + 1")
            .setParameter("cid", centerId)
            .setParameter("start", startDate.atStartOfDay())
            .setParameter("end", endDate.atStartOfDay())
            .getSingleResult();
        sb.append("Total Appointments,").append(total.longValue()).append("\n");

        var completed = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM appointments WHERE center_id = :cid AND status = 'COMPLETED' AND created_at >= :start AND created_at < :end + 1")
            .setParameter("cid", centerId)
            .setParameter("start", startDate.atStartOfDay())
            .setParameter("end", endDate.atStartOfDay())
            .getSingleResult();
        sb.append("Completed,").append(completed.longValue()).append("\n");

        var cancelled = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM appointments WHERE center_id = :cid AND status = 'CANCELLED' AND created_at >= :start AND created_at < :end + 1")
            .setParameter("cid", centerId)
            .setParameter("start", startDate.atStartOfDay())
            .setParameter("end", endDate.atStartOfDay())
            .getSingleResult();
        sb.append("Cancelled,").append(cancelled.longValue()).append("\n");

        @SuppressWarnings("unchecked")
        var outcomeCounts = (List<Object[]>) em.createNativeQuery(
            "SELECT outcome, COUNT(*) FROM appointments WHERE center_id = :cid AND outcome IS NOT NULL AND created_at >= :start AND created_at < :end + 1 GROUP BY outcome")
            .setParameter("cid", centerId)
            .setParameter("start", startDate.atStartOfDay())
            .setParameter("end", endDate.atStartOfDay())
            .getResultList();
        for (var row : outcomeCounts) {
            sb.append("Outcome ").append(row[0]).append(",").append(row[1]).append("\n");
        }

        var totalMl = em.createNativeQuery(
            "SELECT COALESCE(SUM(ml_collected), 0) FROM appointments WHERE center_id = :cid AND ml_collected IS NOT NULL AND created_at >= :start AND created_at < :end + 1")
            .setParameter("cid", centerId)
            .setParameter("start", startDate.atStartOfDay())
            .setParameter("end", endDate.atStartOfDay())
            .getSingleResult();
        sb.append("Total ml Collected,").append(totalMl).append("\n");
    }

    private void appendEmergencySection(StringBuilder sb, Long centerId, LocalDate startDate, LocalDate endDate) {
        sb.append("=== EMERGENCIES ===\n");
        sb.append("Metric,Value\n");

        var total = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM emergency_requests WHERE center_id = :cid AND created_at >= :start AND created_at < :end + 1")
            .setParameter("cid", centerId)
            .setParameter("start", startDate.atStartOfDay())
            .setParameter("end", endDate.atStartOfDay())
            .getSingleResult();
        sb.append("Total Emergencies,").append(total.longValue()).append("\n");

        @SuppressWarnings("unchecked")
        var statusCounts = (List<Object[]>) em.createNativeQuery(
            "SELECT status, COUNT(*) FROM emergency_requests WHERE center_id = :cid AND created_at >= :start AND created_at < :end + 1 GROUP BY status")
            .setParameter("cid", centerId)
            .setParameter("start", startDate.atStartOfDay())
            .setParameter("end", endDate.atStartOfDay())
            .getResultList();
        for (var row : statusCounts) {
            sb.append("Status ").append(row[0]).append(",").append(row[1]).append("\n");
        }
    }

    private void appendDonorResponseSection(StringBuilder sb, Long centerId, LocalDate startDate, LocalDate endDate) {
        sb.append("=== DONOR RESPONSES ===\n");
        sb.append("Metric,Value\n");

        var total = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM donor_responses dr JOIN emergency_requests er ON dr.emergency_id = er.id WHERE er.center_id = :cid AND dr.created_at >= :start AND dr.created_at < :end + 1")
            .setParameter("cid", centerId)
            .setParameter("start", startDate.atStartOfDay())
            .setParameter("end", endDate.atStartOfDay())
            .getSingleResult();
        sb.append("Total Responses,").append(total.longValue()).append("\n");

        @SuppressWarnings("unchecked")
        var statusCounts = (List<Object[]>) em.createNativeQuery(
            "SELECT dr.status, COUNT(*) FROM donor_responses dr JOIN emergency_requests er ON dr.emergency_id = er.id WHERE er.center_id = :cid AND dr.created_at >= :start AND dr.created_at < :end + 1 GROUP BY dr.status")
            .setParameter("cid", centerId)
            .setParameter("start", startDate.atStartOfDay())
            .setParameter("end", endDate.atStartOfDay())
            .getResultList();
        for (var row : statusCounts) {
            sb.append("Status ").append(row[0]).append(",").append(row[1]).append("\n");
        }
    }
}
