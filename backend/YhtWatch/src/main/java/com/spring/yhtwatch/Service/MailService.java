package com.spring.yhtwatch.Service;

import com.spring.yhtwatch.Dto.Response.AvailableJourney;
import com.spring.yhtwatch.Entity.Alert;

import java.util.List;

public interface MailService {
    void sendAlertEmail(Alert alert,  List<AvailableJourney> departures);
}
