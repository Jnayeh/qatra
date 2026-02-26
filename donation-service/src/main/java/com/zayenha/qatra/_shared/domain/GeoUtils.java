package com.zayenha.qatra._shared.domain;

import org.springframework.security.access.prepost.PostAuthorize;

public final class GeoUtils {
    private GeoUtils() {}

    public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1))
                 * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
    @PostAuthorize("returnObject <= 10")
    public static double distanceKmWithLimit(double lat1, double lon1, double lat2, double lon2) {
        return distanceKm(lat1, lon1, lat2, lon2);
    }
}
