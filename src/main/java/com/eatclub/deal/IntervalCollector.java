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
//                System.out.println("Max overlaps: " + maximumOverlaps);
//                System.out.println(intervals.stream()
//                        .map(interval -> String.format("%s - %s - %s - %s", interval.count(), interval.duration(), interval.start(), interval.end()))
//                        .collect(Collectors.joining("\n")));
                return Optional.of(findMaxIntervalsAndMerge(intervals));
            }
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of();
    }

    private Interval findMaxIntervalsAndMerge(SortedSet<Interval> intervals) {
//        new SortedSet<Interval>(List.of(intervals.removeFirst()));
//        System.out.println("Finding max intervals with overlaps: " + maximumOverlaps);
//
//        System.out.println("+++++++++++++++++++++++");
//        System.out.println(intervals.stream()
//                .map(interval -> String.format("%s - %s - %s - %s", interval.count(), interval.duration(), interval.start(), interval.end()))
//                .collect(Collectors.joining("\n")));
//        System.out.println("+++++++++++++++++++++++");

        final SortedSet<Interval> merged = new TreeSet<>(INTERVAL_LARGEST_LONGEST_EARLIEST);
        merged.add(intervals.removeFirst());

//        System.out.println("+++++++++++++++++++++++");
//        System.out.println(intervals.stream()
//                .map(interval -> String.format("%s - %s - %s - %s", interval.count(), interval.duration(), interval.start(), interval.end()))
//                .collect(Collectors.joining("\n")));
//        System.out.println("+++++++++++++++++++++++");

        intervals.stream()
                .filter(count -> count.count() == maximumOverlaps)
                .forEach(interval -> {
//                    System.out.println("Processing interval: " + interval);
                    Interval lastMerged = merged.getLast();
//                    System.out.println("Last merged interval: " + lastMerged);
                    if (interval.end().value().equals(lastMerged.start().value()) ||
                            interval.start().value().equals(lastMerged.end().value())) {
                        merged.remove(lastMerged);
                        merged.add(new Interval(interval.start(), lastMerged.end(), interval.count()));
                    } else {
                        merged.add(interval);
                    }
                });

//        System.out.println("+++++++++++++++++++++++");
//        System.out.println(merged.stream()
//                .map(interval -> String.format("%s - %s - %s - %s", interval.count(), interval.duration(), interval.start(), interval.end()))
//                .collect(Collectors.joining("\n")));
//        System.out.println("+++++++++++++++++++++++");

        return merged.getFirst();
    }
}
