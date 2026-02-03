package com.zayenha.qatra.center.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class DonationCenter {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    private String phone;
    private String email;
    private Double latitude;
    private Double longitude;
    private FacilityType facilityType;
    private OperatingHours operatingHours;
    private CenterStatus status;
    private Integer totalCapacity;
    private Integer maxRegular;
    private Integer slotPeriod;
    private Instant createdAt;
    private Instant updatedAt;

    public DonationCenter() {}

    public DonationCenter(String name, String address, String city, String country,
                          String postalCode, String phone, String email,
                          Double latitude, Double longitude,
                          FacilityType facilityType, OperatingHours operatingHours,
                          Integer totalCapacity, Integer maxRegular, Integer slotPeriod) {
        this.name = name;
        this.address = address;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
        this.phone = phone;
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.facilityType = facilityType;
        this.operatingHours = operatingHours;
        this.status = CenterStatus.PENDING_APPROVAL;
        this.totalCapacity = totalCapacity;
        this.maxRegular = maxRegular;
        this.slotPeriod = slotPeriod;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
}
