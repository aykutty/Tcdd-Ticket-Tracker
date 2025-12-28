package com.spring.yhtwatch.Service.Impl;

import com.spring.yhtwatch.Dto.Response.AvailableJourney;
import com.spring.yhtwatch.Entity.Alert;
import com.spring.yhtwatch.Service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendAlertEmail(Alert alert, List<AvailableJourney> departures) {

        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(alert.getEmail());
        msg.setSubject("TCDD Seat Availability");

        StringBuilder body = new StringBuilder();

        body.append("Seats are available for your route.\n\n");
        body.append("Route: ")
                .append(alert.getOriginStationName())
                .append(" → ")
                .append(alert.getDestinationStationName())
                .append("\n");

        body.append("Date: ")
                .append(alert.getTravelDate())
                .append("\n");

        body.append("Time window: ")
                .append(alert.getStartTime())
                .append(" - ")
                .append(alert.getEndTime())
                .append("\n\n");

        body.append("Available departures:\n\n");

        for (AvailableJourney d : departures) {

            body.append(d.getDepartureTime())
                    .append("\n");

            d.getSeatCounts().forEach((type, count) -> {
                body.append("  ")
                        .append(type.name())
                        .append(": ")
                        .append(count)
                        .append("\n");
            });

            body.append("\n");
        }

        msg.setText(body.toString());

        mailSender.send(msg);
    }

}
