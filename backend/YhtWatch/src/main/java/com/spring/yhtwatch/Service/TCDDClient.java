package com.spring.yhtwatch.Service;

import com.spring.yhtwatch.Dto.Request.TrainAvailabilityRequest;
import com.spring.yhtwatch.Dto.Response.TrainAvailabilityResponse;

public interface TCDDClient {
    TrainAvailabilityResponse checkAvailability(TrainAvailabilityRequest request);
}
