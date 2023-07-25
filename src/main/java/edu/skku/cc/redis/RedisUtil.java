package edu.skku.cc.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final RedisTemplate<String, String> redisTemplate;

    public void saveRefreshToken(String key, String refreshToken, long refreshTokenExpireTime, TimeUnit timeUnit) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("RT: " + key, refreshToken, refreshTokenExpireTime, timeUnit);
        String s = valueOperations.get("RT: " + key);
        log.info("s: {}", s);
    }
}
