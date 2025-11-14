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

    private Counter current = null;
    private int maximumOverlaps = 0;

    @Override
    public Supplier<SortedSet<Interval>> supplier() {
        return () -> new TreeSet<>(Comparator.comparingInt(Interval::count)
                .thenComparing(Interval::duration)
                .thenComparing(Interval::start)
                .thenComparing(Interval::end)
                .reversed());
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
        Interval maximunInterval = intervals.removeFirst();
        Interval currentInterval = maximunInterval;
        for (Interval interval : intervals) {
            if (interval.count() == maximumOverlaps) {
                if (interval.isAdjacentBefore(currentInterval)) {
                    currentInterval = new Interval(interval.start(), currentInterval.end(), currentInterval.count());
                } else {
                    currentInterval = interval;
                }
                if (maximunInterval.duration().getSeconds() < currentInterval.duration().getSeconds()) {
                    maximunInterval = new Interval(currentInterval.start(), currentInterval.end(), currentInterval.count());
                }
            }
        }
        return maximunInterval;
    }
}
