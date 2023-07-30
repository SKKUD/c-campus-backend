package edu.skku.cc.jwt;

import edu.skku.cc.enums.JwtExpirationTime;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenUtil {
    private final long ACCESS_TOKEN_EXPIRATION_TIME = JwtExpirationTime.ACCESS_TOKEN_EXPIRATION_TIME.getExpirationTime();
    private final long REFRESH_TOKEN_EXPIRE_TIME = JwtExpirationTime.REFRESH_TOKEN_EXPIRATION_TIME.getExpirationTime();
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public String createAccessToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))    // JWT 토큰 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION_TIME))    // JWT 토큰 만료 시간
                .signWith(new SecretKeySpec(SECRET_KEY.getBytes(), SignatureAlgorithm.HS512.getJcaName()))   // HS512 알고리즘을 사용하여 secretKey를 이용해 서명
                .compact(); // JWT 토큰 생성
    }

    public String createRefreshToken() {
        return Jwts.builder()
                .setIssuedAt(new Date(System.currentTimeMillis()))    // JWT 토큰 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))    // JWT 토큰 만료 시간
                .signWith(new SecretKeySpec(SECRET_KEY.getBytes(), SignatureAlgorithm.HS512.getJcaName()))   // HS512 알고리즘을 사용하여 secretKey를 이용해 서명
                .compact(); // JWT 토큰 생성
    }

    public Authentication getAuthenticationFromToken(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String userId = String.valueOf(claims.getSubject());
        log.info("userId: {}", userId);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, token);
        log.info("authentication: {}", authentication.getPrincipal());
        return new UsernamePasswordAuthenticationToken(userId, token);
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public long getRefreshTokenExpireTime() {
        return REFRESH_TOKEN_EXPIRE_TIME;
    }
}
