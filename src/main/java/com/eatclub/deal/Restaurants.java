package com.eatclub.deal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.time.LocalTime;
import java.util.List;

public record Restaurants(List<Restaurant> restaurants) {
    public record Time(
            @JsonValue
            @JsonDeserialize(using = LocalTimeDeserializer.class)
            LocalTime value) implements Comparable<Time> {
        @Override
        public int compareTo(Time other) {
            return value.compareTo(other.value());
        }
    }

    public record Deal(
            String objectId,
            int discount,
            boolean dineIn,
            boolean lightning,
            @JsonAlias("start") Time open,
            @JsonAlias("end") Time close,
            int qtyLeft
    ) {
    }

    public record Restaurant(
            String objectId,
            String name,
            String address1,
            String suburb,
            List<String> cuisines,
            List<Deal> deals,
            Time open,
            Time close
    ) {
    }
}
