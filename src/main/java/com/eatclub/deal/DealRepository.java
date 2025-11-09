package com.eatclub.deal;

import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public interface DealRepository {

    Restaurants getRestaurants();

    record Restaurants(List<Restaurant> restaurants) {
        public <T> Stream<T> forEachDeal(BiFunction<Restaurant, Deal, T> function) {
            return Optional.ofNullable(restaurants)
                    .map(Collection::stream)
                    .map(restaurants ->
                            restaurants.flatMap(restaurant -> Optional.ofNullable(restaurant.deals())
                                    .map(deals -> deals.stream()
                                            .map(deal -> function.apply(restaurant, deal))
                                    )
                                    .orElse(Stream.empty()))
                    )
                    .orElse(Stream.empty());
        }
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
