package com.spring.yhtwatch.Dto.Response;

import com.spring.yhtwatch.Enum.TicketType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class AvailableJourney {
    private LocalTime departureTime;
    private Map<TicketType, Integer> seatCounts;
}
