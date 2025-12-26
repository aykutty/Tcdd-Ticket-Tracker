package com.spring.yhtwatch.Service.Impl;

import com.spring.yhtwatch.Repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AlertCleanupService {

    private final AlertRepository alertRepository;

    @Scheduled(cron = "0 0 * * * * ")
    public void cleanup() {

        LocalDateTime cutoff = LocalDateTime.now().minusHours(1);
        alertRepository.deleteOldAlerts(cutoff);
    }
}
