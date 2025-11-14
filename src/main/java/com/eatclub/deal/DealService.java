package com.eatclub.deal;

import com.eatclub.deal.DealMapper.RestaurantDeal;
import com.eatclub.deal.DealRepository.Deal;
import com.eatclub.deal.DealRepository.Restaurant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Service
public class DealService {

    private final DealRepository dealRepository;
    private final DealMapper dealMapper;

    @Autowired
    public DealService(DealRepository dealRepository,
                       DealMapper dealMapper) {
        this.dealRepository = dealRepository;
        this.dealMapper = dealMapper;
    }

    /**
     * Get all active deals that apply for a given time.
     *
     * @param time the time to check for active deals
     * @return a list of active deals
     */
    public List<ActiveDeal> getActiveDeals(LocalTime time) {
        BiFunction<Restaurant, Deal, ActiveDeal> toActiveDealStream = (restaurant, deal) ->
                dealMapper.toActiveDeal(new RestaurantDeal(restaurant, deal));

        return Optional.ofNullable(dealRepository.getRestaurants())
                .map(restaurants -> restaurants.createStream(toActiveDealStream))
                .orElse(Stream.empty())
                .flatMap(ActiveDeal::splitIfRequired)
                .filter(deal ->
                        !time.isAfter(deal.restaurantClose().value()) &&
                        !time.isBefore(deal.restaurantOpen().value())
                )
                .toList();
    }

    /**
     * Get the interval with the highest number of overlapping active deals.
     * <br/><br/>
     * Each active deal is converted to a pair of counters:<br/>
     * - one for the opening time (adding the quantity left)<br/>
     * - one for the closing time (subtracting the quantity left).<br/>
     * <br/><br/>
     * The counters are sorted by time and quantity.
     * <br/><br/>
     * Finally, the sorted counters are re-processed into intervals with counts of overlapping deals.
     * and the new intervals are filtered to find the one with the maximum overlaps and adjacent intervals are merged.
     *
     * @return an Optional containing the peak interval, or empty if there are no deals
     */
    public Optional<Interval> getPeakInterval() {
        BiFunction<Restaurant, Deal, ActiveDeal> toActiveDealStream = (restaurant, deal) ->
                dealMapper.toActiveDeal(new RestaurantDeal(restaurant, deal));

        return Optional.ofNullable(dealRepository.getRestaurants())
                .map(restaurants -> restaurants.createStream(toActiveDealStream))
                .orElse(Stream.empty())
                .flatMap(ActiveDeal::splitIfRequired)
                .flatMap(deal -> Stream.of(
                        new Counter(deal.restaurantOpen(), deal.qtyLeft()),
                        new Counter(deal.restaurantClose(), -deal.qtyLeft())
                ))
                .sorted(Comparator.comparing(Counter::time)
                        .thenComparing(Comparator.comparing(Counter::val).reversed()))
                .collect(new IntervalCollector());
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
        public Stream<ActiveDeal> splitIfRequired() {
            if (restaurantClose.value().isBefore(restaurantOpen.value())) {
                ActiveDeal beforeMidnight = new ActiveDeal(
                        restaurantObjectId,
                        restaurantName,
                        restaurantAddress1,
                        restaurantSuburb,
                        restaurantOpen,
                        new Time(LocalTime.MAX),
                        dealObjectId,
                        discount,
                        dineIn,
                        lightning,
                        qtyLeft
                );
                ActiveDeal afterMidnight = new ActiveDeal(
                        restaurantObjectId,
                        restaurantName,
                        restaurantAddress1,
                        restaurantSuburb,
                        new Time(LocalTime.MIDNIGHT),
                        restaurantClose,
                        dealObjectId,
                        discount,
                        dineIn,
                        lightning,
                        qtyLeft
                );
                return Stream.of(beforeMidnight, afterMidnight);
            } else {
                return Stream.of(this);
            }
        }
    }

    public record Interval(Time start, Time end, Integer count) {
        public Duration duration() {
            if (start.value().isAfter(end.value())) {
                return Duration.ofHours(24).minus(Duration.between(end.value(), start.value()));
            }
            return Duration.between(start.value(), end.value());
        }

        public boolean isAdjacentBefore(Interval other) {
            return (this.end().value().equals(LocalTime.MAX) && other.start().value().equals(LocalTime.MIDNIGHT))
                    || this.end().value().equals(other.start().value());
        }
    }

    public record Counter(Time time, Integer val) {
    }
}
