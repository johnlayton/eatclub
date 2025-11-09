package com.eatclub.deal;

import com.eatclub.deal.DealService.ActiveDeal;
import com.eatclub.deal.DealService.Interval;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureJson
class DealControllerTest {

    @MockitoBean
    private DealService dealService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnDealsValidAtTime() {

        LocalTime time = LocalTime.of(12, 0);

        when(dealService.getActiveDeals(time))
                .thenReturn(List.of(createActiveDeal(), createActiveDeal(), createActiveDeal(), createActiveDeal(), createActiveDeal()));

        WebTestClient client =
                WebTestClient.bindToController(new DealController(dealService))
                        .httpMessageCodecs(configurer ->
                                configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper)))
                        .configureClient()
                        .build();

        client.get()
                .uri("/deals?time=12:00")
                .exchange()
                .expectStatus().isOk()
                .expectAll(
                        spec -> spec.expectStatus().isOk(),
                        spec -> spec.expectHeader().contentType(MediaType.APPLICATION_JSON),
                        spec -> spec.expectBody()
                                .jsonPath("$.deals.length()").isEqualTo(5)
                                .jsonPath("$.deals[0].restaurantObjectId").isEqualTo("restaurantObjectId")
                                .jsonPath("$.deals[0].restaurantOpen").value(Matchers.equalToIgnoringCase("9:00am"))
                                .jsonPath("$.deals[0].restaurantClose").value(Matchers.equalToIgnoringCase("9:00pm"))
                );

        verify(dealService).getActiveDeals(time);
    }

    @Test
    void shouldReturnPeakInterval() {

        when(dealService.getPeakInterval())
                .thenReturn(Optional.of(new Interval(new Time(LocalTime.MIDNIGHT), new Time(LocalTime.MIDNIGHT), 1)));

        WebTestClient client =
                WebTestClient.bindToController(new DealController(dealService))
                        .httpMessageCodecs(configurer ->
                                configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper)))
                        .configureClient()
                        .build();

        client.get()
                .uri("/peak")
                .exchange()
                .expectStatus().isOk()
                .expectAll(
                        spec -> spec.expectStatus().isOk(),
                        spec -> spec.expectHeader().contentType(MediaType.APPLICATION_JSON),
                        spec -> spec.expectBody()
                                .jsonPath("$.peakTimeStart").value(Matchers.equalToIgnoringCase("12:00am"))
                                .jsonPath("$.peakTimeEnd").value(Matchers.equalToIgnoringCase("12:00am"))
                );

        verify(dealService).getPeakInterval();
    }

    @Test
    void shouldReturn404WhenNoPeakInterval() {

        when(dealService.getPeakInterval())
                .thenReturn(Optional.empty());

        WebTestClient client =
                WebTestClient.bindToController(new DealController(dealService))
                        .httpMessageCodecs(configurer ->
                                configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper)))
                        .configureClient()
                        .build();

        client.get()
                .uri("/peak")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectAll(
                        spec -> spec.expectStatus().isNotFound()
                );

        verify(dealService).getPeakInterval();
    }

    private ActiveDeal createActiveDeal() {
        return new ActiveDeal(
                "restaurantObjectId",
                "Restaurant Name",
                "123 Main St",
                "Suburb",
                new Time(LocalTime.of(9, 0)),
                new Time(LocalTime.of(21, 0)),
                "dealObjectId",
                20,
                false,
                true,
                10
        );
    }
}
