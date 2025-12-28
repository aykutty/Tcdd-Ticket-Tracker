package com.spring.yhtwatch.Service;

import com.spring.yhtwatch.Dto.Response.AvailableJourney;
import com.spring.yhtwatch.Entity.Alert;

import java.util.List;

public interface SeatCheckService {
    boolean hasSeats(Alert alert);
    List<AvailableJourney> getAvailableJourneys(Alert alert);
}
