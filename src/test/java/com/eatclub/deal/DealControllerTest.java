package com.eatclub.deal;

import com.eatclub.deal.DealService.Deal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.client.MockMvcWebTestClient;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class DealControllerTest {

    @MockitoBean
    private DealService dealService;


    @Test
    void shouldReturnOffersValidAtTime() {

        LocalTime time = LocalTime.of(12, 0);

        when(dealService.getDeals(time))
                .thenReturn(List.of(createDeal(), createDeal(), createDeal(), createDeal(), createDeal()));

        WebTestClient client =
                MockMvcWebTestClient.bindToController(new DealController(dealService)).build();

        client.get()
                .uri("/deals?time=12:00")
                .exchange()
                .expectStatus().isOk()
                .expectAll(
                        spec -> spec.expectStatus().isOk(),
                        spec -> spec.expectHeader().contentType(MediaType.APPLICATION_JSON),
                        spec -> spec.expectBody()
                                .jsonPath("$.deals.length()").isEqualTo(5)
                );

        verify(dealService).getDeals(time);

    }

    private static Deal createDeal() {
        return new Deal();
    }
}