package com.eatclub.deal;

import com.eatclub.deal.DealService.ActiveDeal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalTime;
import java.util.List;

@RestController
public class DealController {

    private final DealService dealService;

    public DealController(DealService dealService) {
        this.dealService = dealService;
    }

    @GetMapping("/deals")
    public Deals getOffers(@RequestParam("time") LocalTime time) {
        return new Deals(dealService.getActiveDeals(time));
    }
    public record Deals(List<ActiveDeal> deals) {
    }
}
