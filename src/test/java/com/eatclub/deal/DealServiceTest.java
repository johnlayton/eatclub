package com.eatclub.deal;

import com.eatclub.deal.DealRepository.Restaurants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DealServiceTest {

    @InjectMocks
    private DealService dealService;

    @Mock
    private DealRepository dealRepository;

    @Test
    void shouldLoadDealsFromRepository() {
        when(dealRepository.getRestaurants())
                .thenReturn(new Restaurants(List.of()));

        dealService.getDeals(LocalTime.MIDNIGHT);

        verify(dealRepository).getRestaurants();
    }
}