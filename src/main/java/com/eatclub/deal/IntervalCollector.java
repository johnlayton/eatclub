package com.eatclub.deal;

import com.eatclub.deal.DealService.Counter;
import com.eatclub.deal.DealService.Interval;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class IntervalCollector implements Collector<Counter, SortedSet<Interval>, Optional<Interval>> {

    private static final Comparator<Interval> INTERVAL_LARGEST_LONGEST_LATEST =
            Comparator.comparingInt(Interval::count)
                    .thenComparing(Interval::duration)
                    .thenComparing(Interval::start)
                    .thenComparing(Interval::end)
                    .reversed();

    Counter current = null;
    int maximumOverlaps = 0;

    @Override
    public Supplier<SortedSet<Interval>> supplier() {
        return () -> new TreeSet<>(INTERVAL_LARGEST_LONGEST_LATEST);
    }

    @Override
    public BiConsumer<SortedSet<Interval>, Counter> accumulator() {
        return (intervals, next) -> {
            if (current == null) {
                current = next;
                return;
            }
            int val = current.val() + next.val();
            if (next.time().equals(current.time())) {
                current = new Counter(current.time(), val);
                return;
            }
            intervals.add(new Interval(current.time(), next.time(), current.val()));
            maximumOverlaps = Math.max(maximumOverlaps, current.val());
            current = new Counter(next.time(), val);
        };
    }

    @Override
    public BinaryOperator<SortedSet<Interval>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Function<SortedSet<Interval>, Optional<Interval>> finisher() {
        return intervals -> {
            if (intervals.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(findMaxIntervalsAndMerge(intervals));
            }
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }

    private Interval findMaxIntervalsAndMerge(SortedSet<Interval> intervals) {
        final SortedSet<Interval> merged = new TreeSet<>(INTERVAL_LARGEST_LONGEST_LATEST);
        merged.add(intervals.removeFirst());
        intervals.stream()
                .filter(count -> count.count() == maximumOverlaps)
                .forEach(currentInterval -> {
                    Interval lastMergedInterval = merged.getLast();
                    if (currentInterval.isAdjacentBefore(lastMergedInterval)) {
                        merged.remove(lastMergedInterval);
                        merged.add(new Interval(currentInterval.start(), lastMergedInterval.end(), currentInterval.count()));
                    } else {
                        merged.add(currentInterval);
                    }
                });
        return merged.getFirst();
    }
}
