package com.spring.yhtwatch.Service.Impl;

import com.spring.yhtwatch.Dto.Request.AlertRequest;
import com.spring.yhtwatch.Entity.Alert;
import com.spring.yhtwatch.Repository.AlertRepository;
import com.spring.yhtwatch.Service.AlertService;
import com.spring.yhtwatch.Service.MailService;
import com.spring.yhtwatch.Service.SeatCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final SeatCheckService seatCheckService;
    private final MailService mailService;
    private final StationLookupService stationLookupService;

    @Override
    public Alert createAlert(AlertRequest request) {

        Integer originId = stationLookupService.getStationId(request.originStationName());
        Integer destinationId = stationLookupService.getStationId(request.destinationStationName());

        if (originId == null || destinationId == null) {
            throw new IllegalArgumentException("Station not found");
        }

        Alert alert = Alert.builder()
                .email(request.email())
                .originStationId(originId)
                .destinationStationId(destinationId)
                .originStationName(request.originStationName())
                .destinationStationName(request.destinationStationName())
                .travelDate(request.travelDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .minAvailableSeats(request.minAvailableSeats())
                .active(true)
                .build();

        return alertRepository.save(alert);
    }

    @Override
    public void processAlert(Alert alert) {
        boolean hasSeats = seatCheckService.hasSeats(alert);

        if (!hasSeats) {
            if (alert.getLastNotifiedAt() != null) {
                alert.setLastNotifiedAt(null);
                alertRepository.save(alert);
            }
            return;
        }

        if (alert.getLastNotifiedAt() != null) {
            return;
        }

        mailService.sendAlertEmail(alert);
        markNotified(alert);
    }

    @Override
    public void markNotified(Alert alert) {
        alert.setLastNotifiedAt(LocalDateTime.now());
        alertRepository.save(alert);
    }

}
