package com.eatclub.deal;

import com.eatclub.deal.DealRepository.Restaurants;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

@Service
public class DealService {

    private final DealRepository dealRepository;

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public List<Deal> getDeals(LocalTime time) {

        Restaurants restaurants = dealRepository.getRestaurants();

        if (restaurants.restaurants().isEmpty()) {
            return Collections.emptyList();
        }

        return List.of(new Deal());
    }

    public record Offer(
            String restaurantObjectId,
            String restaurantName,
            String restaurantAddress1,
            String restaurantSuburb,
            Time restaurantOpen,
            Time restaurantClose,
            String dealObjectId,
            int discount,
            boolean dineIn,
            boolean lightning
    ) {
    }

    public record Deal() {
    }
}
