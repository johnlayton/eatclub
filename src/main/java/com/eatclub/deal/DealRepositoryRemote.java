package com.eatclub.deal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(value = "deal.repository", havingValue = "remote")
public class DealRepositoryRemote implements DealRepository {

    @Override
    public Restaurants getRestaurants() {
        return new Restaurants(List.of());
    }
}
