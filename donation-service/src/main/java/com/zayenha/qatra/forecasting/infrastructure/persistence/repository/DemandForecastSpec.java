package com.zayenha.qatra.forecasting.infrastructure.persistence.repository;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra.forecasting.infrastructure.persistence.entity.DemandForecastEntity;
import org.springframework.data.jpa.domain.Specification;

public final class DemandForecastSpec {

    private DemandForecastSpec() {
    }

    public static Specification<DemandForecastEntity> bloodType(String bloodType) {
        return (root, query, cb) ->
                bloodType == null || bloodType.isBlank()
                        ? null : cb.equal(root.get("bloodType"), BloodType.valueOf(bloodType));
    }

    public static Specification<DemandForecastEntity> centerId(Long centerId) {
        return (root, query, cb) -> {
            if (centerId == null) return null;
            return cb.equal(root.get("center").get("id"), centerId);
        };
    }

    public static Specification<DemandForecastEntity> city(String city) {
        return (root, query, cb) ->
                city == null || city.isBlank()
                        ? null : cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    public static Specification<DemandForecastEntity> country(String country) {
        return (root, query, cb) ->
                country == null || country.isBlank()
                        ? null : cb.equal(cb.lower(root.get("country")), country.toLowerCase());
    }

    public static Specification<DemandForecastEntity> year(Integer year) {
        return (root, query, cb) ->
                year == null ? null : cb.equal(root.get("year"), year);
    }

    public static Specification<DemandForecastEntity> month(Integer month) {
        return (root, query, cb) ->
                month == null ? null : cb.equal(root.get("month"), month);
    }

    public static Specification<DemandForecastEntity> build(
            String bloodType, Long centerId, String city, String country,
            Integer year, Integer month) {
        Specification<DemandForecastEntity> spec = Specification.allOf();
        if (bloodType != null && !bloodType.isBlank()) spec = spec.and(bloodType(bloodType));
        if (centerId != null) spec = spec.and(centerId(centerId));
        if (city != null && !city.isBlank()) spec = spec.and(city(city));
        if (country != null && !country.isBlank()) spec = spec.and(country(country));
        if (year != null) spec = spec.and(year(year));
        if (month != null) spec = spec.and(month(month));
        return spec;
    }
}
