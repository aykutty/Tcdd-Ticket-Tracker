package com.spring.yhtwatch.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.spring.yhtwatch.Dto.Request.TrainAvailabilityRequest;

public interface TCDDClient {
    JsonNode checkAvailability(TrainAvailabilityRequest request);
}
