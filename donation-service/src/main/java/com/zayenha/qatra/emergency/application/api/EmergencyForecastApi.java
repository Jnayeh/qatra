package com.zayenha.qatra.emergency.application.api;

import com.zayenha.qatra._shared.domain.enums.BloodType;
import com.zayenha.qatra.emergency.application.api.dto.DemandDataPoint;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencyJpaRepository;
import com.zayenha.qatra.emergency.infrastructure.persistence.repository.EmergencySpec;
import com.zayenha.qatra._shared.domain.enums.WeekNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmergencyForecastApi {

    private final EmergencyJpaRepository emergencyJpaRepository;

    @Transactional(readOnly = true)
    public List<DemandDataPoint> getHistoricalDemand(BloodType bloodType, int lookbackMonths) {
        var now = LocalDate.now();
        var startDate = now.minusMonths(lookbackMonths)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        var spec = EmergencySpec.build(bloodType, null, null, null, startDate, Instant.now());
        var emergencies = emergencyJpaRepository.findAll(spec);

        Map<String, int[]> grouped = new LinkedHashMap<>();

        for (var e : emergencies) {
            var center = e.getCenter();
            var date = e.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate();
            var weekNum = WeekNumber.fromDay(date.getDayOfMonth()).getValue();

            var key = e.getBloodType().name()
                    + "|" + center.getId()
                    + "|" + center.getCity()
                    + "|" + center.getCountry()
                    + "|" + date.getYear()
                    + "|" + date.getMonthValue()
                    + "|" + weekNum;

            grouped.merge(key, new int[]{e.getUnitsNeeded(), 1},
                    (existing, incoming) -> new int[]{existing[0] + incoming[0], existing[1] + incoming[1]});
        }

        var result = new ArrayList<DemandDataPoint>();
        for (var entry : grouped.entrySet()) {
            var parts = entry.getKey().split("\\|");
            result.add(new DemandDataPoint(
                    BloodType.valueOf(parts[0]),
                    Long.parseLong(parts[1]),
                    parts[2],
                    parts[3],
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]),
                    entry.getValue()[0],
                    entry.getValue()[1]
            ));
        }
        return result;
    }
}
