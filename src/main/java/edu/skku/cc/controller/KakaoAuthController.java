package edu.skku.cc.controller;

import edu.skku.cc.dto.auth.KakaoLoginSuccessDto;
import edu.skku.cc.dto.auth.LogoutResponseDto;
import edu.skku.cc.dto.jwt.AccessTokenResponseDto;
import edu.skku.cc.exception.CustomException;
import edu.skku.cc.exception.ErrorType;
import edu.skku.cc.service.KakaoAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Controller
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    @Value("${frontend-domain.url}")
    private String authRedirectUrl;
    private final int accessTokenCookieMaxAge = 60 * 30; // 30 mins
    private final int refreshTokenCookieMaxAge = 60 * 60 * 24 * 7; // 7 days
    private final String authRedirectUrl = "https://congcampus.com";

    @GetMapping("/oauth2/callback/kakao")
    public @ResponseBody ResponseEntity kakaoCallback(String code, HttpServletRequest request, HttpServletResponse response) throws Exception {
        KakaoLoginSuccessDto kakaoLoginSuccessDto = kakaoAuthService.kakaoLogin(code);

        log.info("access token created");

        response.addHeader("Location",authRedirectUrl + "/login"
                + "?userId=" +  kakaoLoginSuccessDto.getUserId()
                + "?accessToken=" + kakaoLoginSuccessDto.getAccessToken()
                + "?refreshToken=" + kakaoLoginSuccessDto.getRefreshToken());

        ResponseEntity responseEntity = ResponseEntity.status(HttpStatus.FOUND)
                .body("redirecting to frontend");

        return responseEntity;
    }

    @GetMapping("/oauth2/kakao/logout")
    public @ResponseBody ResponseEntity kakaoLogout(HttpServletRequest request, HttpServletResponse response) {
        log.info("[LOGOUT]");
        Cookie accessTokenCookie = new Cookie("accessToken", "");
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath("/");
        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
//        response.addHeader("Location", authRedirectUrl);
        ResponseEntity responseEntity = ResponseEntity.status(HttpStatusCode.OK)
                .body(new LogoutResponseDto("로그아웃 되었습니다."));
        return responseEntity;
    }

    @PostMapping("/auth/refresh")
    public @ResponseBody ResponseEntity refreshAccessToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String refreshToken = null;

        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("refreshToken")) {
                refreshToken = cookie.getValue();
            }
        }
        String newAccessToken = kakaoAuthService.getNewAccessToken(refreshToken);
        if (newAccessToken == null) {
            throw new CustomException(ErrorType.INVALID_TOKEN_EXCEPTION);
        }
        Cookie accessTokenCookie = new Cookie("accessToken", newAccessToken);
        log.info("new token {}", newAccessToken);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setMaxAge(accessTokenCookieMaxAge); // Cookie 1 expires after 1 hour
        accessTokenCookie.setPath("/");    // Cookie 1 is accessible to all paths
        response.addCookie(accessTokenCookie);
        return ResponseEntity.ok().body(new AccessTokenResponseDto("토큰이 재발급되었습니다."));
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