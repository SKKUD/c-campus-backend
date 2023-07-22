package edu.skku.cc.jwt;

import edu.skku.cc.enums.JwtExpirationTime;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    private long ACCESS_TOKEN_EXPIRATION_TIME = JwtExpirationTime.ACCESS_TOKEN_EXPIRATION_TIME.getExpirationTime();
    private long REFRESH_TOKEN_EXPIRE_TIME = JwtExpirationTime.REFRESH_TOKEN_EXPIRATION_TIME.getExpirationTime();

    public String createAccessToken(String email) {
        return createToken(email, ACCESS_TOKEN_EXPIRATION_TIME);
    }

    public long getRefreshTokenExpireTime() {
        return REFRESH_TOKEN_EXPIRE_TIME;
    }

    public String createRefreshToken(String email) {
        return createToken(email, REFRESH_TOKEN_EXPIRE_TIME);
    }

    private String createToken(String email, long expireTime) {
        Claims claims = Jwts.claims();
        claims.put("email", email);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))    // JWT 토큰 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))    // JWT 토큰 만료 시간
                .signWith(new SecretKeySpec(SECRET_KEY.getBytes(), SignatureAlgorithm.HS512.getJcaName()))   // HS512 알고리즘을 사용하여 secretKey를 이용해 서명
                .compact(); // JWT 토큰 생성
    }
}
