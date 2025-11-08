package com.eatclub.offer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OfferConfiguration {

    @Bean
    public RestClient restClientBuilder() {
        return RestClient.builder().baseUrl("https://eccdn.com.au").build();
    }
}
