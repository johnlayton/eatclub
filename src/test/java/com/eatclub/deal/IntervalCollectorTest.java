package com.eatclub.deal;

import com.eatclub.deal.DealService.Counter;
import com.eatclub.deal.DealService.Interval;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntervalCollectorTest {

    @Test
    void shouldFindSimplePeak() {

        Comparator<Counter> earliestLargest = Comparator.comparing(Counter::time)
                .thenComparing(Counter::val);

        List<Counter> counters = List.of(
                new Counter(new Time(LocalTime.of(9, 0)), 4),
                new Counter(new Time(LocalTime.of(10, 0)), -4),
                new Counter(new Time(LocalTime.of(10, 0)), 5),
                new Counter(new Time(LocalTime.of(11, 0)), -5),
                new Counter(new Time(LocalTime.of(10, 30)), 3),
                new Counter(new Time(LocalTime.of(11, 30)), -3)
        );

        Optional<Interval> max = counters.stream().sorted(earliestLargest).collect(new IntervalCollector());

        assertTrue(max.isPresent(), "Max interval should be present");
        assertEquals(LocalTime.of(10, 30), max.get().start().value(), "Interval start time should be 10:30");
        assertEquals(LocalTime.of(11, 0), max.get().end().value(), "Interval end time should be 11:00");
        assertEquals(8, max.get().count(), "Interval count should be 8");
    }


    @Test
    void shouldFindMultiPeakSameSizePicksLatest() {

        Comparator<Counter> earliestLargest = Comparator.comparing(Counter::time)
                .thenComparing(Counter::val);

        List<Counter> counters = List.of(
                new Counter(new Time(LocalTime.of(9, 0)), 8),
                new Counter(new Time(LocalTime.of(10, 0)), -8),
                new Counter(new Time(LocalTime.of(10, 0)), 5),
                new Counter(new Time(LocalTime.of(11, 0)), -5),
                new Counter(new Time(LocalTime.of(11, 30)), 8),
                new Counter(new Time(LocalTime.of(12, 30)), -8)
        );

        Optional<Interval> max = counters.stream().sorted(earliestLargest).collect(new IntervalCollector());

        assertTrue(max.isPresent(), "Max interval should be present");
        assertEquals(LocalTime.of(11, 30), max.get().start().value(), "Interval start time should be 11:30");
        assertEquals(LocalTime.of(12, 30), max.get().end().value(), "Interval end time should be 12:30");
        assertEquals(8, max.get().count(), "Interval count should be 8");
    }

    @Test
    void shouldFindMultiPeakPicksLongest() {

        Comparator<Counter> earliestLargest = Comparator.comparing(Counter::time)
                .thenComparing(Counter::val);

        List<Counter> counters = List.of(
                new Counter(new Time(LocalTime.of(8, 0)), 8),
                new Counter(new Time(LocalTime.of(10, 0)), -8),
                new Counter(new Time(LocalTime.of(10, 0)), 5),
                new Counter(new Time(LocalTime.of(11, 0)), -5),
                new Counter(new Time(LocalTime.of(12, 0)), 8),
                new Counter(new Time(LocalTime.of(13, 0)), -8)
        );

        Optional<Interval> max = counters.stream().sorted(earliestLargest).collect(new IntervalCollector());

        assertTrue(max.isPresent(), "Max interval should be present");
        assertEquals(LocalTime.of(8, 0), max.get().start().value(), "Interval start time should be 8:00");
        assertEquals(LocalTime.of(10, 0), max.get().end().value(), "Interval end time should be 10:00");
        assertEquals(8, max.get().count(), "Interval count should be 8");
    }

    @Test
    void shouldFindMultiPeakPicksLargest() {

        Comparator<Counter> earliestLargest = Comparator.comparing(Counter::time)
                .thenComparing(Counter::val);

        List<Counter> counters = List.of(
                new Counter(new Time(LocalTime.of(9, 0)), 8),
                new Counter(new Time(LocalTime.of(10, 0)), -8),
                new Counter(new Time(LocalTime.of(10, 0)), 5),
                new Counter(new Time(LocalTime.of(11, 0)), -5),
                new Counter(new Time(LocalTime.of(12, 0)), 9),
                new Counter(new Time(LocalTime.of(13, 0)), -9)
        );

        Optional<Interval> max = counters.stream().sorted(earliestLargest).collect(new IntervalCollector());

        assertTrue(max.isPresent(), "Max interval should be present");
        assertEquals(LocalTime.of(12, 0), max.get().start().value(), "Interval start time should be 12:00");
        assertEquals(LocalTime.of(13, 0), max.get().end().value(), "Interval end time should be 13:00");
        assertEquals(9, max.get().count(), "Interval count should be 8");
    }

    @Test
    void shouldFindMultiPeakMergesAddPicksLongest() {

        Comparator<Counter> earliestLargest = Comparator.comparing(Counter::time)
                .thenComparing(Counter::val);

        List<Counter> counters = List.of(
                new Counter(new Time(LocalTime.of(8, 0)), 8),
                new Counter(new Time(LocalTime.of(9, 0)), -8),
                new Counter(new Time(LocalTime.of(9, 0)), 8),
                new Counter(new Time(LocalTime.of(10, 0)), -8),
                new Counter(new Time(LocalTime.of(10, 0)), 5),
                new Counter(new Time(LocalTime.of(11, 0)), -5),
                new Counter(new Time(LocalTime.of(12, 0)), 8),
                new Counter(new Time(LocalTime.of(13, 0)), -8)
        );

        Optional<Interval> max = counters.stream().sorted(earliestLargest).collect(new IntervalCollector());

        assertTrue(max.isPresent(), "Max interval should be present");
        assertEquals(LocalTime.of(8, 0), max.get().start().value(), "Interval start time should be 8:00");
        assertEquals(LocalTime.of(10, 0), max.get().end().value(), "Interval end time should be 10:00");
        assertEquals(8, max.get().count(), "Interval count should be 8");
    }

    @Test
    void shouldFindMultiPeakMergesAddPicksLatest() {

        Comparator<Counter> earliestLargest = Comparator.comparing(Counter::time)
                .thenComparing(Counter::val);

        List<Counter> counters = List.of(
                new Counter(new Time(LocalTime.of(8, 0)), 8),
                new Counter(new Time(LocalTime.of(9, 0)), -8),
                new Counter(new Time(LocalTime.of(9, 0)), 8),
                new Counter(new Time(LocalTime.of(10, 0)), -8),
                new Counter(new Time(LocalTime.of(10, 0)), 5),
                new Counter(new Time(LocalTime.of(11, 0)), -5),
                new Counter(new Time(LocalTime.of(12, 0)), 8),
                new Counter(new Time(LocalTime.of(13, 0)), -8),
                new Counter(new Time(LocalTime.of(13, 0)), 8),
                new Counter(new Time(LocalTime.of(14, 0)), -8)
        );

        Optional<Interval> max = counters.stream().sorted(earliestLargest).collect(new IntervalCollector());

        assertTrue(max.isPresent(), "Max interval should be present");
        assertEquals(LocalTime.of(12, 0), max.get().start().value(), "Interval start time should be 12:00");
        assertEquals(LocalTime.of(14, 0), max.get().end().value(), "Interval end time should be 14:00");
        assertEquals(8, max.get().count(), "Interval count should be 8");
    }
}