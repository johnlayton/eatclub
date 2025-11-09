package com.eatclub.deal;

import com.eatclub.deal.DealMapper.RestaurantDeal;
import com.eatclub.deal.DealRepository.Deal;
import com.eatclub.deal.DealRepository.Restaurant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.stream.Stream;

@Service
public class DealService {

    private static final Comparator<Interval> INTERVAL_LARGEST_LONGEST_EARLIEST = Comparator.comparingInt(Interval::count)
            .thenComparing(Interval::duration)
            .thenComparing(Interval::start)
            .thenComparing(Interval::end)
            .reversed();
    
    private static final Comparator<Counter> COUNTER_EARLIEST_CLOSED = Comparator.comparing(Counter::time)
            .thenComparing(Counter::val);
    
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
                .filter(deal ->
                        !time.isAfter(deal.restaurantClose().value()) && !time.isBefore(deal.restaurantOpen().value())
                )
                .toList();
    }

    /**
     * Get the interval with the highest number of overlapping active deals.
     * <br/><br/>
     * Each active deal is converted to a pair of counters:<br/>
     *  - one for the opening time (adding the quantity left)<br/>
     *  - one for the closing time (subtracting the quantity left).<br/>
     * <br/><br/>
     * The counters are sorted by time and quantity.
     * <br/><br/>
     * The sorted counters are re-processed into intervals with counts of overlapping deals.
     * <br/><br/>
     * Finally, the new intervals are filtered to find the one with the maximum overlaps and adjacent intervals are merged.
     *
     * @return an Optional containing the peak interval, or empty if there are no deals
     */
    public Optional<Interval> getPeakInterval() {
        BiFunction<Restaurant, Deal, ActiveDeal> toActiveDealStream = (restaurant, deal) ->
                dealMapper.toActiveDeal(new RestaurantDeal(restaurant, deal));

        List<Counter> counters = Optional.ofNullable(dealRepository.getRestaurants())
                .map(restaurants -> restaurants.createStream(toActiveDealStream))
                .orElse(Stream.empty())
                .flatMap(activeDeal -> Stream.of(
                        new Counter(activeDeal.restaurantOpen(), activeDeal.qtyLeft()),
                        new Counter(activeDeal.restaurantClose(), -activeDeal.qtyLeft())
                ))
                .sorted(COUNTER_EARLIEST_CLOSED)
                .toList();

        if (counters.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(findPeakInterval(counters));
        }
    }

    private Interval findPeakInterval(List<Counter> counters) {
        final SortedSet<Interval> intervals = new TreeSet<>(INTERVAL_LARGEST_LONGEST_EARLIEST);
        int maximumOverlaps = 0;
        int currentOverlaps = 0;

        for (int i = 0, j = 1; j < counters.size(); i++, j++) {
            Counter thisEvent = counters.get(i);
            Counter nextEvent = counters.get(j);
            currentOverlaps += thisEvent.val();
            if (!nextEvent.time().equals(thisEvent.time())) {
                intervals.add(new Interval(thisEvent.time(), nextEvent.time(), currentOverlaps));
            }
            maximumOverlaps = Math.max(maximumOverlaps, currentOverlaps);
        }
        return findMaxIntervalsAndMerge(maximumOverlaps, intervals);
    }

    private Interval findMaxIntervalsAndMerge(int maximumOverlaps, SortedSet<Interval> intervals) {
        final List<Interval> merged = new ArrayList<>(List.of(intervals.removeFirst()));

        intervals.stream()
                .filter(count -> count.count() == maximumOverlaps)
                .forEach(interval -> {
                    Interval lastMerged = merged.getLast();
                    if (interval.end().value().isBefore(lastMerged.start().value())) {
                        merged.add(interval);
                    } else {
                        merged.remove(lastMerged);
                        merged.add(new Interval(interval.start(), lastMerged.end(), interval.count()));
                    }
                });
        return merged.getFirst();
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

    public record Interval(Time start, Time end, Integer count) {
        public Duration duration() {
            return Duration.between(start.value(), end.value());
        }
    }

    record Counter(Time time, Integer val) {
    }
}
