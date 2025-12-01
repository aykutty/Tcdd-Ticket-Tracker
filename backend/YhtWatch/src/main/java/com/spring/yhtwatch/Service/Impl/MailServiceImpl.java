package com.spring.yhtwatch.Service.Impl;

import com.spring.yhtwatch.Entity.Alert;
import com.spring.yhtwatch.Service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendAlertEmail(Alert alert) {
        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(alert.getEmail());
        msg.setSubject("TCDD Seat Availability");
        msg.setText("Seat(s) are available for your route.");

        mailSender.send(msg);
    }
}
