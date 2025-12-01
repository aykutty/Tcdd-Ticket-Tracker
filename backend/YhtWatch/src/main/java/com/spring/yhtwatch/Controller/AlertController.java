package com.spring.yhtwatch.Controller;

import com.spring.yhtwatch.Dto.Request.AlertRequest;
import com.spring.yhtwatch.Entity.Alert;
import com.spring.yhtwatch.Service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/alerts")
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    public ResponseEntity<Alert> create(@RequestBody AlertRequest request) {
        Alert alert = alertService.createAlert(request);
        return ResponseEntity.ok(alert);
    }
}
