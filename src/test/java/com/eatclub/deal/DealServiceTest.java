package com.eatclub.deal;

import com.eatclub.deal.DealRepository.Deal;
import com.eatclub.deal.DealRepository.Restaurant;
import com.eatclub.deal.DealRepository.Restaurants;
import com.eatclub.deal.DealService.ActiveDeal;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        DealService.class,
        DealMapperImpl.class
})
class DealServiceTest {

    @Autowired
    @InjectMocks
    private DealService dealService;

    @MockitoBean
    private DealRepository dealRepository;

    @Test
    void shouldReturnEmptyDetailsWhenNoRestaurantsInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(Collections.emptyList()));

        List<ActiveDeal> deals = dealService.getActiveDeals(LocalTime.MIDNIGHT);

        verify(dealRepository).getRestaurants();

        assertTrue(deals.isEmpty(), "Deals list should be empty when repository has no restaurants");
    }

    @Test
    void shouldReturnSingleDetailsWhenSingleRestaurantHasActiveLightningDealInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant(createDeal(true))
                )));

        List<ActiveDeal> deals = dealService.getActiveDeals(LocalTime.of(13, 0));

        verify(dealRepository).getRestaurants();

        assertFalse(deals.isEmpty(), "Deals list should not be empty when the repository has active lightning deals");
        assertEquals(1, deals.size(), "Deals list should contain one deal");
        assertEquals("restaurantObjectId", deals.getFirst().restaurantObjectId(), "Restaurant Object ID should match");
        assertEquals(new Time(LocalTime.of(10, 0)), deals.getFirst().restaurantOpen(), "Restaurant open time should match deal open time");
        assertEquals(new Time(LocalTime.of(14, 0)), deals.getFirst().restaurantClose(), "Restaurant close time should match deal close time");

    }

    @Test
    void shouldReturnEmptyDetailsWhenSingleRestaurantHasNoActiveLightningDealInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant(createDeal(true))
                )));

        List<ActiveDeal> deals = dealService.getActiveDeals(LocalTime.of(9, 0));

        verify(dealRepository).getRestaurants();

        assertTrue(deals.isEmpty(), "Deals list should be empty when the  repository has no active lightning deals");
    }

    @Test
    void shouldReturnSingleDetailsWhenSingleRestaurantHasActiveNoneLightningDealInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant(createDeal(false))
                )));

        List<ActiveDeal> deals = dealService.getActiveDeals(LocalTime.of(9, 0));

        verify(dealRepository).getRestaurants();

        assertFalse(deals.isEmpty(), "Deals list should not be empty when the repository has active non-lightning deals");
        assertEquals(1, deals.size(), "Deals list should contain one deal");
        assertEquals("restaurantObjectId", deals.getFirst().restaurantObjectId());
        assertEquals(new Time(LocalTime.of(9, 0)), deals.getFirst().restaurantOpen(), "Restaurant open time should match restaurant open time");
        assertEquals(new Time(LocalTime.of(21, 0)), deals.getFirst().restaurantClose(), "Restaurant close time should match restaurant close time");
    }

    private static Restaurant createRestaurant(Deal deal) {
        return new Restaurant(
                "restaurantObjectId",
                "Restaurant Name",
                "123 Main St",
                "Suburb",
                Collections.emptyList(),
                List.of(deal),
                new Time(LocalTime.of(9, 0)),
                new Time(LocalTime.of(21, 0))
        );
    }

    private static Deal createDeal(boolean lightning) {
        return new Deal("dealObjectId", 20, false, lightning,
                new Time(LocalTime.of(10, 0)),
                new Time(LocalTime.of(14, 0)),
                10);
    }
}