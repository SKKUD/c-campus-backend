package edu.skku.cc.controller;

import edu.skku.cc.dto.jwt.JwtDto;
import edu.skku.cc.enums.JwtExpirationTime;
import edu.skku.cc.jwt.KakaoAuthenticationFilter;
import edu.skku.cc.jwt.dto.KakaoAccessTokenDto;
import edu.skku.cc.service.KakaoAuthService;
import edu.skku.cc.service.dto.KakaoTokenDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Controller
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private String authRedirectUrl = "http://localhost:3000";

    @GetMapping("/oauth2/callback/kakao")
    public @ResponseBody ResponseEntity kakaoCallback(String code, HttpServletResponse response) throws Exception {
        JwtDto jwtDto = kakaoAuthService.kakaoLogin(code);
        Cookie accessTokenCookie = new Cookie("accessToken", jwtDto.getAccessToken());
        Cookie refreshTokenCookie = new Cookie("refreshToken", jwtDto.getRefreshToken());
        accessTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge(3600); // Cookie 1 expires after 1 hour
        accessTokenCookie.setPath("/");    // Cookie 1 is accessible to all paths
        refreshTokenCookie.setMaxAge(7200); // Cookie 2 expires after 2 hours
        refreshTokenCookie.setPath("/");    // Cookie 2 is accessible to all paths

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);

        response.addHeader("Location", authRedirectUrl);
        addSameSite(response, "Lax");

        ResponseEntity responseEntity = ResponseEntity.status(HttpStatus.FOUND)
                .body("redirecting to frontend");
        return responseEntity;
    }
    
    @PostMapping("/oauth2/kakao/logout")
    public @ResponseBody ResponseEntity kakaoLogout() {
        ResponseEntity re = kakaoAuthService.kakaoLogout();
        log.info("logout response {}", re);
        return re;
    }

    @PostMapping("/oauth2/kakao/refresh")
    public @ResponseBody ResponseEntity kakaoRefresh(@RequestParam("refresh_token") String refreshToken) throws Exception{
        try {
            KakaoAccessTokenDto newAccessToken = kakaoAuthService.getNewKakaoAccessToken(refreshToken);
            return ResponseEntity.ok().body(newAccessToken);
        } catch (HttpClientErrorException.BadRequest e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void addSameSite(HttpServletResponse response, String sameSite) {
        Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
        boolean firstHeader = true;
        for (String header : headers) {
            if (firstHeader) {
                response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; Secure; %s", header, "SameSite=" + sameSite));
                firstHeader = false;
                continue;
            }
            response.addHeader(HttpHeaders.SET_COOKIE, String.format("%s; Secure; %s", header, "SameSite=" + sameSite));
        }
    }
}