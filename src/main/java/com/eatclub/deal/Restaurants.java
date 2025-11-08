package com.eatclub.deal;

import java.util.List;

public record Restaurants(List<Restaurant> restaurants) {
    public record Restaurant(
    ) {}
}
