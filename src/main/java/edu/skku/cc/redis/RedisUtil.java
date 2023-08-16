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

    public void saveRefreshToken(String refreshToken, String value, long refreshTokenExpireTime, TimeUnit timeUnit) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(refreshToken, value, refreshTokenExpireTime, timeUnit);
        log.info(valueOperations.get(refreshToken));
    }

    public String validateRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            return null;
        }
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String userId = valueOperations.get(refreshToken);
        log.info("userId received from refreshToken {}", userId);
        return userId;
    }
}
