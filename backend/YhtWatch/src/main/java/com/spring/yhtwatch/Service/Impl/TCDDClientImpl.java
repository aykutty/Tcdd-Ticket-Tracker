package com.spring.yhtwatch.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.spring.yhtwatch.Service.TCDDClient;
import com.spring.yhtwatch.Dto.Request.TrainAvailabilityRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
