package com.zayenha.qatra.emergency.infrastructure.persistence.repository;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra.emergency.infrastructure.persistence.entity.EmergencyRequestEntity;
import jakarta.persistence.criteria.Join;
import com.zayenha.qatra.center.infrastructure.persistence.entity.CenterEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public final class EmergencySpec {

    private EmergencySpec() {
    }

    public static Specification<EmergencyRequestEntity> bloodType(BloodType bloodType) {
        return (root, query, cb) ->
                bloodType == null ? null : cb.equal(root.get("bloodType"), bloodType);
    }

    public static Specification<EmergencyRequestEntity> centerId(Long centerId) {
        return (root, query, cb) -> {
            if (centerId == null) return null;
            Join<EmergencyRequestEntity, CenterEntity> join = root.join("center");
            return cb.equal(join.get("id"), centerId);
        };
    }

    public static Specification<EmergencyRequestEntity> city(String city) {
        return (root, query, cb) -> {
            if (city == null || city.isBlank()) return null;
            Join<EmergencyRequestEntity, CenterEntity> join = root.join("center");
            return cb.equal(cb.lower(join.get("city")), city.toLowerCase());
        };
    }

    public static Specification<EmergencyRequestEntity> country(String country) {
        return (root, query, cb) -> {
            if (country == null || country.isBlank()) return null;
            Join<EmergencyRequestEntity, CenterEntity> join = root.join("center");
            return cb.equal(cb.lower(join.get("country")), country.toLowerCase());
        };
    }

    public static Specification<EmergencyRequestEntity> fromDate(Instant fromDate) {
        return (root, query, cb) ->
                fromDate == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate);
    }

    public static Specification<EmergencyRequestEntity> toDate(Instant toDate) {
        return (root, query, cb) ->
                toDate == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), toDate);
    }

    public static Specification<EmergencyRequestEntity> build(
            BloodType bloodType, Long centerId, String city, String country,
            Instant fromDate, Instant toDate) {
        Specification<EmergencyRequestEntity> spec = Specification.allOf();
        if (bloodType != null) spec = spec.and(bloodType(bloodType));
        if (centerId != null) spec = spec.and(centerId(centerId));
        if (city != null && !city.isBlank()) spec = spec.and(city(city));
        if (country != null && !country.isBlank()) spec = spec.and(country(country));
        if (fromDate != null) spec = spec.and(fromDate(fromDate));
        if (toDate != null) spec = spec.and(toDate(toDate));
        return spec;
    }
}
