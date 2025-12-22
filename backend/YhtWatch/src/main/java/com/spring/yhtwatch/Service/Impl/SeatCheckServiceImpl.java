package com.spring.yhtwatch.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.yhtwatch.Dto.Request.PassengerTypeCount;
import com.spring.yhtwatch.Dto.Request.SearchRoute;
import com.spring.yhtwatch.Service.TCDDClient;
import com.spring.yhtwatch.Dto.Request.TrainAvailabilityRequest;
import com.spring.yhtwatch.Entity.Alert;
import com.spring.yhtwatch.Service.SeatCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatCheckServiceImpl implements SeatCheckService {

    private final TCDDClient tcddClient;
    private final StationLookupService stationLookupService;

    @Override
    public boolean hasSeats(Alert alert) {
        JsonNode root = tcddClient.checkAvailability(buildRequest(alert));
        int seats = extractSeats(root);
        return seats >= alert.getMinAvailableSeats();
    }

    private int extractSeats(JsonNode root) {
        if (root == null) return 0;

        int total = 0;

        for (JsonNode leg : root.path("trainLegs")) {
            for (JsonNode availability : leg.path("trainAvailabilities")) {

                for (JsonNode train : availability.path("trains")) {
                    for (JsonNode cap : train.path("bookingClassCapacities")) {
                        int id = cap.path("bookingClassId").asInt();
                        int capVal = cap.path("capacity").asInt(0);
                        if (!isDisabledBookingClass(id)) {
                            total += capVal;
                        }
                    }
                }

                for (JsonNode car : availability.path("cars")) {
                    for (JsonNode av : car.path("availabilities")) {
                        String cabin = av.path("cabinClass").path("name").asText("");
                        if (!"ENGELLİ".equalsIgnoreCase(cabin)) {
                            total += av.path("availability").asInt(0);
                        }
                    }
                }
            }
        }
        return total;
    }

    private boolean isDisabledBookingClass(int id) {
        return id == 7;
    }

    private TrainAvailabilityRequest buildRequest(Alert alert) {
        Integer departureId =
                stationLookupService.getStationId(alert.getOriginStationName());
        Integer arrivalId =
                stationLookupService.getStationId(alert.getDestinationStationName());

        LocalDateTime localMidnight =
                alert.getTravelDate().atStartOfDay();

        String dateTime =
                localMidnight.format(
                        DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
                );


        return new TrainAvailabilityRequest(
                List.of(new SearchRoute(
                        departureId,
                        alert.getOriginStationName(),
                        arrivalId,
                        alert.getDestinationStationName(),
                        dateTime
                )),
                List.of(new PassengerTypeCount(0, 1)),
                false
        );
    }
}
