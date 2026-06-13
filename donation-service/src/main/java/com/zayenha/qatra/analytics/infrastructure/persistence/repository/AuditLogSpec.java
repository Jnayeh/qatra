package com.zayenha.qatra.analytics.infrastructure.persistence.repository;

import com.zayenha.qatra.analytics.infrastructure.persistence.entity.AuditLogEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterAdminProfileEntity;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterStaffProfileEntity;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class AuditLogSpec {

    private AuditLogSpec() {
    }

    public static Specification<AuditLogEntity> action(String action) {
        return (root, query, cb) ->
                action == null ? null : cb.equal(root.get("action"), action);
    }

    public static Specification<AuditLogEntity> fromDate(Instant fromDate) {
        return (root, query, cb) ->
                fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("timestamp"), fromDate);
    }

    public static Specification<AuditLogEntity> toDate(Instant toDate) {
        return (root, query, cb) ->
                toDate == null ? null : cb.lessThanOrEqualTo(root.get("timestamp"), toDate);
    }

    public static Specification<AuditLogEntity> centerId(Long centerId) {
        return (root, query, cb) -> {
            if (centerId == null) return null;

            Subquery<Long> adminUsers = query.subquery(Long.class);
            var cap = adminUsers.from(CenterAdminProfileEntity.class);
            adminUsers.select(cap.get("user").get("id"));
            adminUsers.where(cb.equal(cap.get("center").get("id"), centerId));

            Subquery<Long> staffUsers = query.subquery(Long.class);
            var csp = staffUsers.from(CenterStaffProfileEntity.class);
            staffUsers.select(csp.get("user").get("id"));
            staffUsers.where(cb.equal(csp.get("center").get("id"), centerId));

            return cb.or(
                    cb.in(root.get("user").get("id")).value(adminUsers),
                    cb.in(root.get("user").get("id")).value(staffUsers)
            );
        };
    }

    public static Specification<AuditLogEntity> build(String action, Instant fromDate, Instant toDate, Long centerId) {
        Specification<AuditLogEntity> spec = Specification.allOf();
        if (action != null) spec = spec.and(action(action));
        if (fromDate != null) spec = spec.and(fromDate(fromDate));
        if (toDate != null) spec = spec.and(toDate(toDate));
        if (centerId != null) spec = spec.and(centerId(centerId));
        return spec;
    }
}
