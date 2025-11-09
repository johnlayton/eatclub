package com.eatclub.deal;

import com.eatclub.deal.DealService.ActiveDeal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping(value = "/deals")
    public Deals getDeals(@RequestParam("time") LocalTime time) {
        return new Deals(dealService.getActiveDeals(time));
    }

    @GetMapping(value = "/peak")
    public ResponseEntity<Peak> getPeak() {
        return dealService.getPeakInterval()
                .map(p -> new ResponseEntity<>(new Peak(p.start(), p.end()), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public record Peak(Time peakTimeStart, Time peakTimeEnd) {
    }

    public record Deals(List<ActiveDeal> deals) {
    }
}
