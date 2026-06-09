package com.zayenha.qatra._config;

import io.micrometer.observation.ObservationPredicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;

@Slf4j
@Configuration
public class ObservationFilterConfig {

    private static final String ACTUATOR_PREFIX = "/actuator";

    @Bean
    ObservationPredicate actuatorObservationPredicate() {
        return (observation, context) -> {
            if (context instanceof ServerRequestObservationContext ctx && ctx.getCarrier() != null) {
              String path = ctx.getCarrier().getRequestURI();
              log.info("Observing request: {}", path);
                return !path.startsWith(ACTUATOR_PREFIX);
            }
            return true;
        };
    }

}
