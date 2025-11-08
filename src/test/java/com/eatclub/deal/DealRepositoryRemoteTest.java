package com.eatclub.deal;

import com.eatclub.deal.DealRepository.Deal;
import com.eatclub.deal.DealRepository.Restaurant;
import com.eatclub.deal.DealRepository.Restaurants;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = {
        DealRepositoryRemote.class,
})
@TestPropertySource(properties = {"deal.repository=remote"})
class DealRepositoryRemoteTest {

    @Autowired
    private DealRepository dealRepository;

    @Test
    void shouldLoadRemoteRepository() {
        Restaurants restaurants = dealRepository.getRestaurants();
        assertEquals(6, restaurants.restaurants().size(),
                "Should load 6 restaurants from the local JSON file");

        Restaurant restaurant = restaurants.restaurants().getFirst();
        assertEquals("Masala Kitchen", restaurant.name(),
                "The first restaurant should be Masala Kitchen");

        Deal deal = restaurant.deals().getFirst();
        assertEquals(50, deal.discount(),
                "The first deal of the first restaurant should have a 50 discount");
        assertFalse(deal.dineIn(),
                "The first deal of the first restaurant should not be dine-in");
        assertEquals(LocalTime.of(15, 0), deal.open().value(),
                "The first deal of the first restaurant should open at 3:00pm");
    }

}