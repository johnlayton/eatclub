package com.eatclub.deal;

import com.eatclub.deal.DealMapper.RestaurantDeal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class DealService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DealService.class);

    private final DealRepository dealRepository;
    private final DealMapper dealMapper;

    @Autowired
    public DealService(DealRepository dealRepository,
                       DealMapper dealMapper) {
        this.dealRepository = dealRepository;
        this.dealMapper = dealMapper;
    }

    public List<ActiveDeal> getActiveDeals(LocalTime time) {
        final Time timeWrapper = new Time(time);
        return dealRepository.getRestaurants().restaurants()
                .stream()
                .flatMap(restaurant -> restaurant.deals().stream()
                        .filter(deal -> {
                            return deal.lightning()
                                    ? (!timeWrapper.value().isAfter(deal.close().value()) && !timeWrapper.value().isBefore(deal.open().value()))
                                    : (!timeWrapper.value().isAfter(restaurant.close().value()) && !timeWrapper.value().isBefore(restaurant.open().value()));
                        })
                        .map(deal -> {
                            LOGGER.trace("Restaurant {} has an active deal: {}", restaurant, deal);
                            return deal;
                        })
                        .map(deal -> {
                            return dealMapper.toActiveDeal(new RestaurantDeal(restaurant, deal));
                        })
                        .map(deal -> {
                            LOGGER.trace("Found an active deal: {}", deal);
                            return deal;
                        })
                )
                .toList();
    }

    public record ActiveDeal(
            String restaurantObjectId,
            String restaurantName,
            String restaurantAddress1,
            String restaurantSuburb,
            Time restaurantOpen,
            Time restaurantClose,
            String dealObjectId,
            int discount,
            boolean dineIn,
            boolean lightning,
            int qtyLeft
    ) {
    }
}
