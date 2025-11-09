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

    private static final Comparator<Interval> INTERVAL_LARGEST_LONGEST_EARLIEST =
            Comparator.comparingInt(Interval::count)
                    .thenComparing(Interval::duration)
                    .thenComparing(Interval::start)
                    .thenComparing(Interval::end)
                    .reversed();

    Counter currentCounter = null;
    int maximumOverlaps = 0;
    int currentOverlaps = 0;

    @Override
    public Supplier<SortedSet<Interval>> supplier() {
        return () -> new TreeSet<>(INTERVAL_LARGEST_LONGEST_EARLIEST);
    }

    @Override
    public BiConsumer<SortedSet<Interval>, Counter> accumulator() {
        return (intervals, counter) -> {
            if (currentCounter != null) {
                currentOverlaps += currentCounter.val();
                if (!counter.time().equals(currentCounter.time())) {
                    intervals.add(new Interval(currentCounter.time(), counter.time(), currentOverlaps));
                }
            }
            maximumOverlaps = Math.max(maximumOverlaps, currentOverlaps);
            currentCounter = counter;
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
        final SortedSet<Interval> merged = new TreeSet<>(INTERVAL_LARGEST_LONGEST_EARLIEST);
        merged.add(intervals.removeFirst());
        intervals.stream()
                .filter(count -> count.count() == maximumOverlaps)
                .forEach(currentInterval -> {
                    Interval lastMergedInterval = merged.getLast();
                    if (currentInterval.end().value().equals(lastMergedInterval.start().value()) ||
                            currentInterval.start().value().equals(lastMergedInterval.end().value())) {
                        merged.remove(lastMergedInterval);
                        merged.add(new Interval(currentInterval.start(), lastMergedInterval.end(), currentInterval.count()));
                    } else {
                        merged.add(currentInterval);
                    }
                });
        return merged.getFirst();
    }
}
