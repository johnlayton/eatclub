package com.eatclub.deal;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = DealRepositoryRemote.class)
@TestPropertySource(properties = {"deal.repository=remote"})
class DealRepositoryRemoteTest {

    @Autowired
    private DealRepository dealRepository;

    @Test
    @Disabled
    void shouldLoadRemoteRepository() {
        Restaurants restaurants = dealRepository.getRestaurants();
        assertEquals(6, restaurants.restaurants().size(), "Should load 6 restaurants from the local JSON file");
    }

}