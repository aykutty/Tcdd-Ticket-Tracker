package com.spring.yhtwatch.Service;

import com.spring.yhtwatch.Dto.Request.AlertRequest;
import com.spring.yhtwatch.Entity.Alert;

import java.util.List;

public interface AlertService {

    Alert createAlert(AlertRequest request);

    void processAlert(Alert alert);

    void markNotified(Alert alert);

}
