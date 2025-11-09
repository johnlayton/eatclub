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
        /* Create a stream of T by applying the given BiFunction to each combination of Restaurant and Deal.
         *
         * @param function A BiFunction that takes a Restaurant and a Deal and produces an instance of T.
         * @param <T>      The type of the elements in the resulting stream.
         * @return A Stream of T created by applying the function to each Restaurant-Deal pair.
         */
        public <T> Stream<T> createStream(BiFunction<Restaurant, Deal, T> function) {
            return Optional.ofNullable(restaurants)
                    .map(Collection::stream).stream()
                    .flatMap(restaurants ->
                            restaurants.flatMap(restaurant -> restaurant.createStream(function)));
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
        /* Create a stream of T by applying the given BiFunction to this Restaurant and each of its Deals.
         *
         * @param function A BiFunction that takes a Restaurant and a Deal and produces an instance of T.
         * @param <T>      The type of the elements in the resulting stream.
         * @return A Stream of T created by applying the function to this Restaurant and each Deal.
         */
        public <T> Stream<T> createStream(BiFunction<Restaurant, Deal, T> function) {
            return Optional.ofNullable(deals)
                    .map(Collection::stream).stream()
                    .flatMap(deals ->
                            deals.map(deal -> function.apply(Restaurant.this, deal)));
        }
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
