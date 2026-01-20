package com.spring.yhtwatch.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.spring.yhtwatch.Service.TCDDClient;
import com.spring.yhtwatch.Dto.Request.TrainAvailabilityRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class TCDDClientImpl implements TCDDClient {

    private final WebClient tcddWebClient;

    @Value("${tcdd.token}")
    private String bearerToken;

    @Override
    public JsonNode checkAvailability(TrainAvailabilityRequest request) {

        return tcddWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/tms/train/train-availability")
                        .queryParam("environment", "prod")
                        .queryParam("userId", "1")
                        .build()
                )
                .header("Authorization", "Bearer " + bearerToken)
                .header("User-Agent", "Mozilla/5.0")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("unit-id", "3895")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }


}
