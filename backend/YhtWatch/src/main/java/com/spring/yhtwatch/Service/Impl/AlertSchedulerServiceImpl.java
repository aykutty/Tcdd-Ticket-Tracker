package com.spring.yhtwatch.Service.Impl;

import com.spring.yhtwatch.Entity.Alert;
import com.spring.yhtwatch.Repository.AlertRepository;
import com.spring.yhtwatch.Service.AlertService;
import com.spring.yhtwatch.Service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertSchedulerServiceImpl {

    private final AlertService alertService;
    private final AlertRepository alertRepository;

    @Scheduled(fixedRate = 300000)
    public void checkAlerts() {

        List<Alert> alerts = alertRepository.findByActiveTrue();

        for (Alert alert : alerts) {
            alertService.processAlert(alert);
        }
    }
}
