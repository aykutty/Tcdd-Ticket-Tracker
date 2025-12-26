package com.spring.yhtwatch.Service.Impl;

import com.spring.yhtwatch.Service.RedisService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final StringRedisTemplate redis;
    private static final Logger log = LoggerFactory.getLogger(RedisService.class);


    @Override
    public Optional<Boolean> getCachedAvailability(String key) {
        String v = redis.opsForValue().get(key);
        return v == null ? Optional.empty() : Optional.of(Boolean.parseBoolean(v));
    }

    @Override
    public void cacheAvailability(String key, boolean available) {
        redis.opsForValue().set(
                key,
                String.valueOf(available),
                Duration.ofMinutes(1)
        );
    }

}
