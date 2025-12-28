package com.spring.yhtwatch.Service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.spring.yhtwatch.Dto.Request.PassengerTypeCount;
import com.spring.yhtwatch.Dto.Request.SearchRoute;
import com.spring.yhtwatch.Dto.Response.AvailableJourney;
import com.spring.yhtwatch.Enum.TicketType;
import com.spring.yhtwatch.Service.RedisService;
import com.spring.yhtwatch.Service.TCDDClient;
import com.spring.yhtwatch.Dto.Request.TrainAvailabilityRequest;
import com.spring.yhtwatch.Entity.Alert;
import com.spring.yhtwatch.Service.SeatCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeatCheckServiceImpl implements SeatCheckService {

    private final TCDDClient tcddClient;
    private final StationLookupService stationLookupService;
    private final RedisService redisService;

    @Override
    public boolean hasSeats(Alert alert) {

        String route =
                alert.getOriginStationName() + "→" +
                        alert.getDestinationStationName();

        String window =
                alert.getStartTime() + "-" + alert.getEndTime();

        String key = buildRedisKey(alert);

        Optional<Boolean> cached = redisService.getCachedAvailability(key);
        if (cached.isPresent()) {
            log.info(
                    "alertId={} route={} window={} Using cached availability={}",
                    alert.getId(),
                    route,
                    window,
                    cached.get()
            );
            return cached.get();
        }

        JsonNode root = tcddClient.checkAvailability(buildRequest(alert));

        List<AvailableJourney> departures =
                findAvailableDepartures(root, alert);

        boolean available = !departures.isEmpty();

        log.info(
                "alertId={} route={} window={} availableCount={} departures={}",
                alert.getId(),
                route,
                window,
                departures.size(),
                departures
        );

        redisService.cacheAvailability(key, available);

        return available;
    }

    private TrainAvailabilityRequest buildRequest(Alert alert) {
        Integer departureId =
                stationLookupService.getStationId(alert.getOriginStationName());
        Integer arrivalId =
                stationLookupService.getStationId(alert.getDestinationStationName());

        String dateTime = alert.getTravelDate().atStartOfDay()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

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

    private List<AvailableJourney> findAvailableDepartures(
            JsonNode root,
            Alert alert
    ) {

        int originId = stationLookupService.getStationId(alert.getOriginStationName());
        int destinationId = stationLookupService.getStationId(alert.getDestinationStationName());

        List<AvailableJourney> results = new ArrayList<>();

        for (JsonNode leg : root.path("trainLegs")) {
            for (JsonNode availability : leg.path("trainAvailabilities")) {
                for (JsonNode train : availability.path("trains")) {

                    if (!matchesRouteOrdered(train, originId, destinationId)) {
                        continue;
                    }

                    LocalTime departureTime =
                            getOriginDepartureTimeFromSegments(train, originId);

                    if (departureTime == null ||
                            !isWithinTimeWindow(alert, departureTime)) {
                        continue;
                    }

                    Map<TicketType, Integer> seatCounts =
                            collectSeatCounts(train);

                    results.add(
                            new AvailableJourney(departureTime, seatCounts)
                    );

                    logSeatSummary(alert, departureTime, seatCounts);
                }
            }
        }

        return results;
    }

    private boolean matchesRouteOrdered(JsonNode train, int originId, int destinationId) {

        int originIndex = -1;
        int destinationIndex = -1;

        int i = 0;
        for (JsonNode seg : train.path("segments")) {
            int depId = seg.path("segment").path("departureStation").path("id").asInt(-1);
            int arrId = seg.path("segment").path("arrivalStation").path("id").asInt(-1);

            if (originIndex < 0 && depId == originId) {
                originIndex = i;
            }

            if (arrId == destinationId) {
                destinationIndex = i;
            }
            i++;
        }

        return originIndex >= 0 && destinationIndex >= 0 && originIndex <= destinationIndex;
    }

    @Override
    public List<AvailableJourney> getAvailableJourneys(Alert alert) {
        JsonNode root = tcddClient.checkAvailability(buildRequest(alert));
        return findAvailableDepartures(root, alert);
    }

    private LocalTime getOriginDepartureTimeFromSegments(JsonNode train, int originId) {

        ZoneId tz = ZoneId.of("Europe/Istanbul");

        for (JsonNode seg : train.path("segments")) {
            int depId = seg.path("segment").path("departureStation").path("id").asInt(-1);
            if (depId != originId) continue;

            long depMillis = seg.path("departureTime").asLong(0);
            if (depMillis <= 0) return null;

            return Instant.ofEpochMilli(depMillis)
                    .atZone(tz)
                    .toLocalTime();
        }

        return null;
    }

    private boolean isWithinTimeWindow(Alert alert, LocalTime departureTime) {
        return departureTime != null
                && !departureTime.isBefore(alert.getStartTime())
                && !departureTime.isAfter(alert.getEndTime());
    }

    private Map<TicketType, Integer> collectSeatCounts(JsonNode train) {

        Map<TicketType, Integer> counts = new EnumMap<>(TicketType.class);

        for (JsonNode cabin : train.path("cabinClassAvailabilities")) {

            int cabinId = cabin.path("cabinClass").path("id").asInt();
            int count = cabin.path("availabilityCount").asInt(0);

            TicketType type = mapCabinIdToTicketType(cabinId);

            counts.merge(type, count, Integer::sum);
        }

        return counts;
    }

    private TicketType mapCabinIdToTicketType(int cabinId) {
        return switch (cabinId) {
            case 1 -> TicketType.BUSINESS;
            case 12 -> TicketType.DISABLED;
            default -> TicketType.ECONOMY;
        };
    }

    private String buildRedisKey(Alert alert) {
        return "tcdd:" +
                alert.getOriginStationName().replace(" ", "_") + ":" +
                alert.getDestinationStationName().replace(" ", "_") + ":" +
                alert.getTravelDate() + ":" +
                alert.getStartTime() + ":" +
                alert.getEndTime();
    }

    private void logSeatSummary(
            Alert alert,
            LocalTime time,
            Map<TicketType, Integer> seats
    ) {

        log.info(
                "alertId={} {}→{} {} ECONOMY={} BUSINESS={} DISABLED={}",
                alert.getId(),
                alert.getOriginStationName(),
                alert.getDestinationStationName(),
                time,
                seats.getOrDefault(TicketType.ECONOMY, 0),
                seats.getOrDefault(TicketType.BUSINESS, 0),
                seats.getOrDefault(TicketType.DISABLED, 0)
        );
    }
}


