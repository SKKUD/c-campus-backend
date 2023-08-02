package edu.skku.cc.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

//expiration
//invalid token
//
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private String TOKEN_VALIDATION_URL = "https://kapi.kakao.com/v1/user/access_token_info";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");
        String kakaoAccessToken = parseBearerToken(bearerToken);

        log.info("bearerToken {}", bearerToken);
        log.info("kakaoAccessToken {}", kakaoAccessToken);

        ResponseEntity<String> kakaoTokenValidationResponse = getKakaoTokenValidationResponse(kakaoAccessToken);
        if (kakaoTokenValidationResponse.getStatusCode() == HttpStatus.OK) {
            log.info("kakaoTokenValidationResponse {}", kakaoTokenValidationResponse);
            Authentication authentication = jwtTokenUtil.getAuthenticationFromKakaoToken(kakaoAccessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("authentication.getPrincipal: {}", authentication.getPrincipal());
        }
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ResponseEntity<String> getKakaoTokenValidationResponse(String kakaoAccessToken) {
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoAccessToken);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> kakaoTokenValidationResponse = rt.exchange(
                    TOKEN_VALIDATION_URL,
                    HttpMethod.GET,
                    httpEntity,
                    String.class
            );
            log.info("status code {}", kakaoTokenValidationResponse.getStatusCode());
            return kakaoTokenValidationResponse;
        } catch (HttpClientErrorException.Unauthorized e) {
            log.info("Exception: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (HttpClientErrorException.BadRequest e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
    private String parseBearerToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}