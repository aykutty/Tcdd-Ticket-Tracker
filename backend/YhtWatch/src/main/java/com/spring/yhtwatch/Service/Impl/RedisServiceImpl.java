package com.spring.yhtwatch.Service.Impl;

import com.spring.yhtwatch.Service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redis;
    private static final Logger log = LoggerFactory.getLogger(RedisService.class);

    public boolean shouldProcess(UUID alertId) {

        String key = "alert" + alertId;

        Boolean exists = redis.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            log.info("REDIS — Alert {} skipped (cached request)", alertId);
            return false;
        }

        redis.opsForValue().set(key, "1", Duration.ofMinutes(5));
        log.info("REDIS — Alert {} processed (no cache entry, created new)", alertId);
        return true;
    }
}
