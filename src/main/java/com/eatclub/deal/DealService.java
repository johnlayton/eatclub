package com.eatclub.deal;

import com.eatclub.deal.DealMapper.RestaurantDeal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
                        .filter(deal -> deal.lightning()
                                ? (!timeWrapper.value().isAfter(deal.close().value()) && !timeWrapper.value().isBefore(deal.open().value()))
                                : (!timeWrapper.value().isAfter(restaurant.close().value()) && !timeWrapper.value().isBefore(restaurant.open().value())))
                        .map(deal -> dealMapper.toActiveDeal(new RestaurantDeal(restaurant, deal)))
                )
                .toList();
    }

    public Optional<Interval> getPeakInterval() {
        List<Counter> counters = dealRepository.getRestaurants().restaurants()
                .stream()
                .flatMap(restaurant -> restaurant.deals().stream()
                        .map(deal -> deal.lightning()
                                ? new Interval(deal.open(), deal.close(), deal.qtyLeft())
                                : new Interval(restaurant.open(), restaurant.close(), deal.qtyLeft()))
                )
                .flatMap(interval -> Stream.of(
                        new Counter(interval.start, interval.count),
                        new Counter(interval.end, -interval.count)
                ))
                .sorted(Comparator
                        .comparing(Counter::time)
                        .thenComparing(Counter::val))
                .toList();
        if (counters.isEmpty()) {
            return Optional.empty();
        } else {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Counters; \n{}", counters.stream().map(Counter::toString).collect(Collectors.joining("\n")));
            }
            return Optional.of(find(counters));
        }
    }

    private Interval find(List<Counter> counters) {
        int maximumOverlaps = 0;
        int currentOverlaps = 0;

        SortedSet<Interval> intervals = new TreeSet<>(
                Comparator.comparingInt(Interval::count)
                        .thenComparing(Interval::duration)
                        .thenComparing(Interval::start)
                        .thenComparing(Interval::end)
                        .reversed());

        for (int i = 0, j = 1; j < counters.size(); i++, j++) {
            Counter thisEvent = counters.get(i);
            Counter nextEvent = counters.get(j);
            currentOverlaps += thisEvent.val();
            if (!nextEvent.time().equals(thisEvent.time())) {
                intervals.add(new Interval(thisEvent.time(), nextEvent.time(), currentOverlaps));
            }
            maximumOverlaps = Math.max(maximumOverlaps, currentOverlaps);
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Intervals; \n{}", intervals.stream().map(Interval::toString).collect(Collectors.joining("\n")));
        }

        return findMaxAndMerge(maximumOverlaps, intervals);
    }

    private Interval findMaxAndMerge(int maximumOverlaps, SortedSet<Interval> intervals) {
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
