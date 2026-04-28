package com.zayenha.qatra._shared.web.serializer;

import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@JacksonComponent
public class FlexibleLocalTimeDeserializer extends StdDeserializer<LocalTime> {

    private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm[:ss]");
    private static final DateTimeFormatter H_MM = DateTimeFormatter.ofPattern("H:mm[:ss]");

    public FlexibleLocalTimeDeserializer() {
        super(LocalTime.class);
    }

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) {
        var text = p.getString().trim();
        try {
            return LocalTime.parse(text, HH_MM_SS);
        } catch (DateTimeParseException e) {
            return LocalTime.parse(text, H_MM);
        }
    }
}
