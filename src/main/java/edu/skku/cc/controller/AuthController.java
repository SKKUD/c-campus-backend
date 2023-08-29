package edu.skku.cc.controller;

import edu.skku.cc.dto.auth.AuthenticationResponseDto;
import edu.skku.cc.jwt.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtTokenUtil jwtTokenUtil;

    //    @GetMapping("oauth2/kakao/logout")
//    public String logout(HttpServletRequest request, HttpServletResponse response) {
//        return "redirect:https://congcampus.com";
//    }
    @GetMapping("/auth/authentication")
    public ResponseEntity<AuthenticationResponseDto> checkAuthentication(HttpServletRequest request, Authentication authentication) {
        String authorizationHeaderValue = request.getHeader("Authorization");
        String accessToken = null;
        String bearerToken = null;
        log.info("authorizationHeaderValue {}", authorizationHeaderValue);
        if (authorizationHeaderValue != null) {
            bearerToken = authorizationHeaderValue;
            accessToken = parseBearerToken(bearerToken);
        }

        log.info("accessToken value: {}", accessToken);

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

    private String parseBearerToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}