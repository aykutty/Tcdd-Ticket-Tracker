package com.spring.yhtwatch.Service;

import java.util.Optional;
import java.util.UUID;

public interface RedisService {

    public Optional<Boolean> getCachedAvailability(String key);
    public void cacheAvailability(String key, boolean available);
}
