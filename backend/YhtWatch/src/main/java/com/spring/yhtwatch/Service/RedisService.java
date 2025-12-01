package com.spring.yhtwatch.Service;

import java.util.UUID;

public interface RedisService {

    boolean shouldProcess(UUID alertId);
}
