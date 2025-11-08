package com.eatclub.deal;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.List;

public interface DealRepository {

    Restaurants getRestaurants();

    record Restaurants(List<Restaurant> restaurants) {
    }

    record Restaurant(
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

    record Deal(
            String objectId,
            int discount,
            boolean dineIn,
            boolean lightning,
            @JsonAlias("start") Time open,
            @JsonAlias("end") Time close,
            int qtyLeft
    ) {
    }
}
