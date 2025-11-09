package com.eatclub.deal;

import com.eatclub.deal.DealRepository.Deal;
import com.eatclub.deal.DealRepository.Restaurant;
import com.eatclub.deal.DealRepository.Restaurants;
import com.eatclub.deal.DealService.ActiveDeal;
import com.eatclub.deal.DealService.Interval;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

        assertTrue(deals.isEmpty(), "Deals list should be empty when the repository has no restaurants");
    }

    @Test
    void shouldReturnEmptyDetailsWhenMissingRestaurantsInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(null));

        List<ActiveDeal> deals = dealService.getActiveDeals(LocalTime.MIDNIGHT);

        verify(dealRepository).getRestaurants();

        assertTrue(deals.isEmpty(), "Deals list should be empty when the repository has missing restaurants");
    }

    @Test
    void shouldReturnEmptyDetailsWhenRestaurantIsMissingDealsInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant()
                )));

        List<ActiveDeal> deals = dealService.getActiveDeals(LocalTime.MIDNIGHT);

        verify(dealRepository).getRestaurants();

        assertTrue(deals.isEmpty(), "Deals list should be empty when the repository has missing deals for the restaurant");
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

    @Test
    void shouldReturnEmptyIntervalWhenWhenNoRestaurantsInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of()));

        Optional<Interval> interval = dealService.getPeakInterval();

        verify(dealRepository).getRestaurants();

        assertFalse(interval.isPresent(), "Interval should be empty when repository has no restaurants");
    }

    @Test
    void shouldReturnEmptyIntervalWhenWhenRestaurantsIsMissingInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(null));

        Optional<Interval> interval = dealService.getPeakInterval();

        verify(dealRepository).getRestaurants();

        assertFalse(interval.isPresent(), "Interval should be empty when the repository has missing restaurants");
    }

    @Test
    void shouldReturnEmptyIntervalWhenWhenRestaurantDealsIsMissingInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant()
                )));

        Optional<Interval> interval = dealService.getPeakInterval();

        verify(dealRepository).getRestaurants();

        assertFalse(interval.isPresent(), "Interval should be empty when the repository has restaurants with missing deals");
    }

    @Test
    void shouldReturnIntervalWhenSingleRestaurantHasActiveNoneLightningDealInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant(createDeal(false))
                )));

        Optional<Interval> interval = dealService.getPeakInterval();

        verify(dealRepository).getRestaurants();

        assertTrue(interval.isPresent(), "Interval should be present when repository has restaurants with non-lightning deals");
        assertEquals(new Time(LocalTime.of(9, 0)), interval.get().start(), "Interval start time should be 9:00 AM");
        assertEquals(new Time(LocalTime.of(21, 0)), interval.get().end(), "Interval end time should be 9:00 PM");
    }

    @Test
    void shouldReturnIntervalWhenSingleRestaurantHasMultipleActiveLightningDealInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant(
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(11, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(12, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(11, 0)), new Time(LocalTime.of(13, 0)), 10)
                        )
                )));

        Optional<Interval> interval = dealService.getPeakInterval();

        verify(dealRepository).getRestaurants();

        assertTrue(interval.isPresent(), "Interval should be present when repository has restaurants with non-lightning deals");
        assertEquals(new Time(LocalTime.of(10, 0)), interval.get().start(), "Interval start time should be 9:00 AM");
        assertEquals(new Time(LocalTime.of(12, 0)), interval.get().end(), "Interval end time should be 9:00 PM");
    }

    @Test
    void shouldReturnIntervalWhenSingleRestaurantHasMultipleActiveUnbalancedLightningDealInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant(
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(11, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(12, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(11, 0)), new Time(LocalTime.of(13, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(15, 0)), new Time(LocalTime.of(16, 0)), 100)
                        )
                )));

        Optional<Interval> interval = dealService.getPeakInterval();

        verify(dealRepository).getRestaurants();

        assertTrue(interval.isPresent(), "Interval should be present when the repository has restaurants with unbalanced lightning deals");
        assertEquals(new Time(LocalTime.of(15, 0)), interval.get().start(), "Interval start time should be 3:00 PM");
        assertEquals(new Time(LocalTime.of(16, 0)), interval.get().end(), "Interval end time should be 4:00 PM");
    }

    @Test
    void shouldReturnIntervalWhenMultipleRestaurantHasMultipleActiveUnbalancedLightningDealInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant(
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(11, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(12, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(11, 0)), new Time(LocalTime.of(13, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(16, 0)), new Time(LocalTime.of(18, 0)), 80)
                        ),
                        createRestaurant(
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(11, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(12, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(11, 0)), new Time(LocalTime.of(13, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(15, 0)), new Time(LocalTime.of(16, 0)), 50)
                        )
                )));

        Optional<Interval> interval = dealService.getPeakInterval();

        verify(dealRepository).getRestaurants();

        assertTrue(interval.isPresent(), "Interval should be present when the repository has restaurants with unbalanced lightning deals");
        assertEquals(new Time(LocalTime.of(16, 0)), interval.get().start(), "Interval start time should be 6:00 PM");
        assertEquals(new Time(LocalTime.of(18, 0)), interval.get().end(), "Interval end time should be 8:00 PM");
    }

    @Test
    void shouldReturnIntervalWhenSingleRestaurantHasMultipleActiveIsolatedLightningDealInRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of(
                        createRestaurant(
                                createDeal(true, new Time(LocalTime.of(8, 0)), new Time(LocalTime.of(9, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(8, 0)), new Time(LocalTime.of(9, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(8, 0)), new Time(LocalTime.of(9, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(11, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(12, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(12, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(11, 0)), new Time(LocalTime.of(13, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(15, 0)), new Time(LocalTime.of(16, 0)), 10),
                                createDeal(true, new Time(LocalTime.of(15, 0)), new Time(LocalTime.of(17, 0)), 10)
                        )
                )));

        Optional<Interval> interval = dealService.getPeakInterval();

        verify(dealRepository).getRestaurants();

        assertTrue(interval.isPresent(), "Interval should be present when repository has restaurants with non-lightning deals");
        assertEquals(new Time(LocalTime.of(10, 0)), interval.get().start(), "Interval start time should be 9:00 AM");
        assertEquals(new Time(LocalTime.of(12, 0)), interval.get().end(), "Interval end time should be 9:00 PM");
    }

    private static Restaurant createRestaurant() {
        return new Restaurant(
                "restaurantObjectId",
                "Restaurant Name",
                "123 Main St",
                "Suburb",
                Collections.emptyList(),
                null,
                new Time(LocalTime.of(9, 0)),
                new Time(LocalTime.of(21, 0))
        );
    }

    private static Restaurant createRestaurant(Deal... deal) {
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
        return createDeal(lightning, new Time(LocalTime.of(10, 0)), new Time(LocalTime.of(14, 0)), 10);
    }

    private static Deal createDeal(boolean lightning, Time open, Time close, int qtyLeft) {
        return new Deal("dealObjectId", 20, false, lightning, open, close, qtyLeft);
    }
}