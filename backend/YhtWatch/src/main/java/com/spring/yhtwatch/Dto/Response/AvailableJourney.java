package com.spring.yhtwatch.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalTime;

@Data
@AllArgsConstructor
public class AvailableJourney {
    private LocalTime departureTime;
}
