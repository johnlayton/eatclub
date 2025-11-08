package com.eatclub.deal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.Charset;

@Service
@ConditionalOnProperty(value = "deal.repository", havingValue = "local", matchIfMissing = true)
public class DealRepositoryLocal implements DealRepository {

    private final Resource data;
    private final ObjectMapper objectMapper;

    public DealRepositoryLocal(@Value("classpath:challengedata.json") Resource data,
                               ObjectMapper objectMapper) {
        this.data = data;
        this.objectMapper = objectMapper;
    }

    @Override
    public Restaurants getRestaurants() {
        try {
            return objectMapper.readValue(data.getContentAsString(Charset.defaultCharset()), Restaurants.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
