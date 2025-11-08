package com.eatclub.deal;

import com.eatclub.deal.DealRepository.Deal;
import com.eatclub.deal.DealRepository.Restaurant;
import com.eatclub.deal.DealRepository.Restaurants;
import com.eatclub.deal.DealService.ActiveDeal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @InjectMocks
    private DealService dealService;

    @Mock
    private DealRepository dealRepository;

    @Test
    void shouldReturnEmptyDetailsWhenNoRestaurantsInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(Collections.emptyList()));

        List<ActiveDeal> deals = dealService.getDeals(LocalTime.MIDNIGHT);

        verify(dealRepository).getRestaurants();

        assertTrue(deals.isEmpty(), "Deals list should be empty when repository has no restaurants");
    }

    @Test
    void shouldReturnSingleDetailsWhenSingleRestaurantHasActiveLightningDealInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        new Restaurant(
                                "restaurantObjectId",
                                "Restaurant Name",
                                "123 Main St",
                                "Suburb",
                                Collections.emptyList(),
                                List.of(
                                        new Deal(
                                                "dealObjectId",
                                                20,
                                                false,
                                                true,
                                                new Time(LocalTime.of(10, 0)),
                                                new Time(LocalTime.of(14, 0)),
                                                5
                                        )
                                ),
                                new Time(LocalTime.of(9, 0)),
                                new Time(LocalTime.of(21, 0))
                        )
                )));

        List<ActiveDeal> deals = dealService.getDeals(LocalTime.of(13, 0));

        verify(dealRepository).getRestaurants();

        assertFalse(deals.isEmpty(), "Deals list should not be empty when repository has active lightning deals");
        assertEquals(1, deals.size(), "Deals list should contain one deal");
        assertEquals("restaurantObjectId", deals.getFirst().restaurantObjectId());
    }

    private static Deal createDeal() {
        return new Deal("objectId", 10, false, true,
                new Time(LocalTime.of(11, 0)),
                new Time(LocalTime.of(14, 0)),
                10);
    }
}