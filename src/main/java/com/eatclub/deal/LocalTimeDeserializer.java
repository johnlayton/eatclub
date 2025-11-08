package com.eatclub.deal;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LocalTimeDeserializer  extends JsonDeserializer<LocalTime> {

    private final DateTimeFormatter formatter;

    public LocalTimeDeserializer() {
        this.formatter = DateTimeFormatter.ofPattern("h:mma");
    }

    @Override
    public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return LocalTime.parse(p.getText(), formatter);
    }
}