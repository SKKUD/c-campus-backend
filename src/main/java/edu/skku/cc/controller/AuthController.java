package edu.skku.cc.controller;

import edu.skku.cc.dto.auth.AuthenticationResponseDto;
import edu.skku.cc.jwt.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/auth/authentication")
    public ResponseEntity<AuthenticationResponseDto> checkAuthentication(HttpServletRequest request, Authentication authentication) {
        String accessToken = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println(cookie.getName());
                if (cookie.getName().equals("accessToken")) {
                    accessToken = cookie.getValue();
                }
            }
        }

        if (StringUtils.hasText(accessToken) && jwtTokenUtil.validateToken(accessToken)) {
            String stringUserId = String.valueOf(authentication.getPrincipal());
            try {
                return ResponseEntity.status(HttpStatusCode.OK).body(new AuthenticationResponseDto(UUID.fromString(stringUserId)));
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatusCode.OK).body(new AuthenticationResponseDto(null));
            }
        }

        return ResponseEntity.status(HttpStatusCode.OK).body(new AuthenticationResponseDto(null));
    }
}