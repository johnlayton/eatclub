package com.eatclub.deal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

import java.time.LocalTime;

public record Time(@JsonValue LocalTime value) implements Comparable<Time> {
    @Override
    public int compareTo(Time other) {
        return value.compareTo(other.value());
    }
}
