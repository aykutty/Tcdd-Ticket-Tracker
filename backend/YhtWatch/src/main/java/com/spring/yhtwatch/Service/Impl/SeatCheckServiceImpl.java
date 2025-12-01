package com.spring.yhtwatch.Service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.yhtwatch.Dto.Request.PassengerTypeCount;
import com.spring.yhtwatch.Dto.Request.SearchRoute;
import com.spring.yhtwatch.Service.TCDDClient;
import com.spring.yhtwatch.Dto.Request.TrainAvailabilityRequest;
import com.spring.yhtwatch.Dto.Response.TrainAvailabilityResponse;
import com.spring.yhtwatch.Entity.Alert;
import com.spring.yhtwatch.Service.SeatCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZoneOffset;
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

        TrainAvailabilityRequest req = buildRequest(alert);

        try {
            String json = new ObjectMapper().writeValueAsString(req);
            log.info("TCDD REQUEST => " + json);
        } catch (Exception e) {
            log.error("Failed to log request", e);
        }

        TrainAvailabilityResponse res = tcddClient.checkAvailability(req);

        int seats = res.totalSeats();

        log.info("TCDD RESPONSE => totalSeats = " + seats);

        if (seats == 0) {
            log.info("No available seats for alert " + alert.getId());
        } else {
            log.info("Available seats found for alert " + alert.getId() +
                    ": required = " + alert.getMinAvailableSeats() +
                    ", found = " + seats);
        }

        return seats >= alert.getMinAvailableSeats();
    }


    private TrainAvailabilityRequest buildRequest(Alert alert) {
        Integer departureId = stationLookupService.getStationId(alert.getOriginStationName());
        Integer arrivalId   = stationLookupService.getStationId(alert.getDestinationStationName());

        String dateTime = alert.getTravelDate()
                .atStartOfDay()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        String departureName = stationLookupService.getExactStationName(departureId);
        String arrivalName   = stationLookupService.getExactStationName(arrivalId);

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
