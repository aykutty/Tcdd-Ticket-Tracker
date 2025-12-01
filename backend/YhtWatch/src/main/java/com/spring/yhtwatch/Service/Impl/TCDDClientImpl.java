package com.spring.yhtwatch.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.spring.yhtwatch.Service.TCDDClient;
import com.spring.yhtwatch.Dto.Request.TrainAvailabilityRequest;
import com.spring.yhtwatch.Dto.Response.TrainAvailabilityResponse;
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
    public TrainAvailabilityResponse checkAvailability(TrainAvailabilityRequest request) {

        JsonNode root = tcddWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/tms/train/train-availability")
                        .queryParam("environment", "prod")
                        .queryParam("userId", "1")
                        .build()
                )
                .header("Authorization", "Bearer " + bearerToken)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36")
                //.header("Origin", "https://ebilet.tcddtasimacilik.gov.tr")
                //.header("Referer", "https://ebilet.tcddtasimacilik.gov.tr/")
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/plain, */*")
                .header("unit-id", "3895")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class).flatMap(body -> {
                            System.out.println("TCDD ERROR RESPONSE: " + body);
                            return Mono.error(new RuntimeException("TCDD API Error: " + resp.statusCode()));
                        })
                )
                .bodyToMono(JsonNode.class)
                .block();

        int seats = extractSeats(root);
        return new TrainAvailabilityResponse(seats);
    }

    private int extractSeats(JsonNode root) {
        if (root == null) {
            log.warn("TCDD response body is NULL");
            return 0;
        }

        int total = 0;

        for (JsonNode leg : root.path("trainLegs")) {
            for (JsonNode availability : leg.path("trainAvailabilities")) {

                for (JsonNode train : availability.path("trains")) {
                    String trainName = train.path("trainName").asText("UNKNOWN");
                    log.info("Checking train: " + trainName);

                    for (JsonNode cap : train.path("bookingClassCapacities")) {
                        int id = cap.path("bookingClassId").asInt();
                        int capVal = cap.path("capacity").asInt(0);

                        log.info(" - Booking class " + id + " capacity = " + capVal);

                        if (isDisabledBookingClass(id)) continue;

                        total += capVal;
                    }
                }

                for (JsonNode car : availability.path("cars")) {
                    for (JsonNode av : car.path("availabilities")) {

                        String cabin = av.path("cabinClass").path("name").asText("");
                        int avail = av.path("availability").asInt(0);

                        log.info(" - Cabin " + cabin + " availability = " + avail);

                        if (cabin.equalsIgnoreCase("ENGELLİ")) continue;

                        total += avail;
                    }
                }
            }
        }

        log.info("Total seat count extracted: " + total);
        return total;
    }



        private boolean isDisabledBookingClass(int id) {
        return id == 7;
    }

}
