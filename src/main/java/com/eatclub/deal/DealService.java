package com.eatclub.deal;

import com.eatclub.deal.DealController.Deals;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class DealService {

    private final DealRepository dealRepository;

    public DealService(DealRepository dealRepository) {
        this.dealRepository = dealRepository;
    }

    public List<Deals> getDeals(LocalTime time) {
        return List.of(new Deals(List.of()));
    }
}
