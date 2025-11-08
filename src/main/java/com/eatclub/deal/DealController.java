package com.eatclub.deal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;

@RestController
public class DealController {
    public record Deal() {
    }

    public record Deals(List<Deal> deals) {
    }

    @GetMapping("/deals")
    public Deals getOffers(@RequestParam LocalTime time) {
        return new Deals(List.of(new Deal(), new Deal(), new Deal(), new Deal(), new Deal()));
    }
}
