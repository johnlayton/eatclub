package com.eatclub.deal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnProperty(value = "deal.repository", havingValue = "remote")
public class DealRepositoryRemote implements DealRepository {

    private final RestClient restClient;

    public DealRepositoryRemote(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://eccdn.com.au")
                .build();
    }

    @Override
    public Restaurants getRestaurants() {
        return restClient.get().uri("/misc/challengedata.json").retrieve().body(Restaurants.class);
    }
}
