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
import java.util.function.Function;
import java.util.function.Predicate;
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

    public List<ActiveDeal> getActiveDeals(LocalTime time) {
        return dealRepository.getRestaurants().restaurants()
                .stream()
                .flatMap(restaurant -> restaurant.deals().stream()
                        .filter(isDealApplicable(restaurant, new Time(time)))
                        .map(mapDealToActiveDeal(restaurant))
                )
                .toList();
    }

    public Optional<Interval> getPeakInterval() {
        List<Counter> counters = dealRepository.getRestaurants().restaurants()
                .stream()
                .flatMap(restaurant -> restaurant.deals().stream()
                        .map(mapToInterval(restaurant))
                        .flatMap(interval -> Stream.of(
                                new Counter(interval.start, interval.count),
                                new Counter(interval.end, -interval.count)
                        ))
                )
                .sorted(Comparator
                        .comparing(Counter::time)
                        .thenComparing(Counter::val))
                .toList();
        if (counters.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(find(counters));
        }
    }

    private Function<Deal, ActiveDeal> mapDealToActiveDeal(Restaurant restaurant) {
        return deal -> dealMapper.toActiveDeal(new RestaurantDeal(restaurant, deal));
    }

    private Predicate<Deal> isDealApplicable(Restaurant restaurant, Time time) {
        return deal -> deal.lightning()
                ? (!time.value().isAfter(deal.close().value()) && !time.value().isBefore(deal.open().value()))
                : (!time.value().isAfter(restaurant.close().value()) && !time.value().isBefore(restaurant.open().value()));
    }

    private Function<Deal, Interval> mapToInterval(Restaurant restaurant) {
        return deal -> deal.lightning()
                ? new Interval(deal.open(), deal.close(), deal.qtyLeft())
                : new Interval(restaurant.open(), restaurant.close(), deal.qtyLeft());
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
