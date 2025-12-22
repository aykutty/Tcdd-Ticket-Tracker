package com.spring.yhtwatch.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.spring.yhtwatch.Service.TCDDClient;
import com.spring.yhtwatch.Dto.Request.TrainAvailabilityRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/plain, */*")
                .header("unit-id", "3895")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class).flatMap(body -> {

                            log.error("TCDD ERROR RESPONSE: {}", body);

                            if (body.contains("\"code\":604")) {
                                log.info("TCDD returned 604 (no trains).");
                                return Mono.empty();
                            }

                            return Mono.error(
                                    new RuntimeException("TCDD API Error: " + resp.statusCode())
                            );
                        })
                )
                .bodyToMono(JsonNode.class)
                .block();
    }

}
