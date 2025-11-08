package com.eatclub.offer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

@SpringBootTest
class OfferControllerTest {

    WebTestClient client =
            MockMvcWebTestClient.bindToController(new OfferController()).build();

    @Test
    void shouldReturnOffersValidAtTime() {
        client.get()
                .uri("/deals?time=12:30")
                .exchange()
                .expectStatus().isOk()
                .expectAll(
                        spec -> spec.expectStatus().isOk(),
                        spec -> spec.expectHeader().contentType(MediaType.APPLICATION_JSON),
                        spec -> spec.expectBody()
                                .jsonPath("$.deals.length()").isEqualTo(5)
                );

    }
}