package com.eatclub.deal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(value = "deal.repository", havingValue = "remote")
public class DealRepositoryRemote implements DealRepository {

    @Autowired
    private RestClient restClient;

    @Override
    public Restaurants getRestaurants() {
        return restClient.get().uri("/misc/challengedata.json").retrieve().body(Restaurants.class);
    }
}
