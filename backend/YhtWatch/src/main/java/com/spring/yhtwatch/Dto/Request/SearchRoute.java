package com.spring.yhtwatch.Dto.Request;

public record SearchRoute(
        Integer departureStationId,
        String departureStationName,
        Integer arrivalStationId,
        String arrivalStationName,
        String departureDate
) {
}
