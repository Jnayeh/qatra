package com.zayenha.qatra.emergency.application.api.dto;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandDataPoint {
    private BloodType bloodType;
    private Long centerId;
    private String centerCity;
    private String centerCountry;
    private int year;
    private int month;
    private Integer weekNumber;
    private int totalUnits;
    private long requestCount;
}
