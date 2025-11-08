package com.eatclub.deal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@ConditionalOnProperty(value = "deal.repository", havingValue = "local", matchIfMissing = true)
public class DealRepositoryLocal implements DealRepository {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:challengedata.json")
    private Resource data;


    @Override
    public Restaurants getRestaurants() {
        try {
            return objectMapper.readValue(data.getURL(), Restaurants.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
