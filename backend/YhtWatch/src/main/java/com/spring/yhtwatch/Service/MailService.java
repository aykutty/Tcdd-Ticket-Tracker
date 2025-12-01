package com.spring.yhtwatch.Service;

import com.spring.yhtwatch.Entity.Alert;

public interface MailService {
    void sendAlertEmail(Alert alert);
}
