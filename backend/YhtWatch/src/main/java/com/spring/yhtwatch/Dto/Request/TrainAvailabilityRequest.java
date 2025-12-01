package com.spring.yhtwatch.Dto.Request;

import java.util.List;

public record TrainAvailabilityRequest(
        List<SearchRoute> searchRoutes,
        List<PassengerTypeCount> passengerTypeCounts,
        boolean searchReservation
) {
}
