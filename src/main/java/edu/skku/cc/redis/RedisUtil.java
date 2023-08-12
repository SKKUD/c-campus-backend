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

    public String getRefreshToken(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String refreshToken = valueOperations.get(key);
        log.info("received refreshToken {}", refreshToken);
        return refreshToken;
    }
    // why does it need key
    // where do i get key from
    // refresh token existence
    // refresh token validation

    //front -> request w/ refreshToken -> back -> validate refreshToken by redis?

    // front -> credential login -> success -> backend callback -> response

    // cookie
    // response body

    // problem: (1)kakao auth success -> not possible to redirect with response body
    // solutions:
        // - (1) response body: maybe not possible
        // - (2) cookie -> problem: dk why not being created in elasticbeanstalk: same-site even not working (then ssh?..) yeah..
        // - (3) session (?)
        // - (4) url
        // - header -> either not working
        //
}
