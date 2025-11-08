package com.eatclub.deal;

import com.eatclub.deal.Restaurants.Deal;
import com.eatclub.deal.Restaurants.Restaurant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest(classes = DealRepositoryLocal.class)
@AutoConfigureJson
@TestPropertySource(properties = {"deal.repository=local"})
class DealRepositoryLocalTest {

    @Autowired
    private DealRepository dealRepository;


    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:challengedata.json")
    private Resource data;


    @Test
    void shouldParseLocalJsonFile() {
        try {
            objectMapper.readValue(data.getURL(), Restaurants.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldLoadLocalRepository() {
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

    @Test
    void shouldHandleJsonAliasForDealDuration() {
        Restaurants restaurants = dealRepository.getRestaurants();

        Restaurant restaurant = restaurants.restaurants().get(3);

        assertEquals("Kekou", restaurant.name(),
                "The first restaurant should be Kekou");

        Deal deal1 = restaurant.deals().getFirst();

        assertEquals(LocalTime.of(14, 0), deal1.open().value(),
                "The first deal of the first restaurant should open at 2:00pm");

        assertEquals(LocalTime.of(21, 0), deal1.close().value(),
                "The first deal of the first restaurant should close at 9:00pm");

        Deal deal2 = restaurant.deals().get(1);

        assertEquals(LocalTime.of(17, 0), deal2.open().value(),
                "The second deal of the first restaurant should open at 5:00pm");

        assertEquals(LocalTime.of(21, 0), deal2.close().value(),
                "The second deal of the first restaurant should close at 3:00pm");
    }
}