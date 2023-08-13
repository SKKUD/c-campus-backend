package edu.skku.cc.controller;

import edu.skku.cc.jwt.dto.KakaoAccessTokenDto;
import edu.skku.cc.service.KakaoAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Controller
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private String authRedirectUrl = "http://localhost:3000";

    @GetMapping("/oauth2/callback/kakao")
    public @ResponseBody ResponseEntity kakaoCallback(String code, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Long userId = kakaoAuthService.kakaoLogin(code);
        request.getSession().setAttribute("userId", userId);

        response.addHeader("Location", authRedirectUrl);
        ResponseEntity responseEntity = ResponseEntity.status(HttpStatus.FOUND)
                .body("redirecting to frontend");
        return responseEntity;
    }

    @PostMapping("/oauth2/kakao/logout")
    public @ResponseBody ResponseEntity kakaoLogout(HttpServletRequest request) {
        ResponseEntity re = kakaoAuthService.kakaoLogout();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
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