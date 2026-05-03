package com.zayenha.qatra.center.application.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CenterDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
}
