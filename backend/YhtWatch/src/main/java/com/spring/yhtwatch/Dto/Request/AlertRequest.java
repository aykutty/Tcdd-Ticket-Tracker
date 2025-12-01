package com.spring.yhtwatch.Dto.Request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record AlertRequest(

        @Email
        @NotBlank
        String email,

        @NotBlank
        String originStationName,

        @NotBlank
        String destinationStationName,

        @NotNull
        LocalDate travelDate,

        @NotNull
        LocalTime startTime,

        @NotNull
        LocalTime endTime,

        @Min(1)
        Integer minAvailableSeats
) { }
