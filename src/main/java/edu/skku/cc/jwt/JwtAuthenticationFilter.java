package edu.skku.cc.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String TOKEN_VALIDATION_URL = "https://kapi.kakao.com/v1/user/access_token_info";
        String contentType = "application/x-www-form-urlencoded;charset=utf-8";
        String grantType = "authorization_code";

        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        HttpServletRequest httpServletRequest = request;
        String bearerToken = httpServletRequest.getHeader("Authorization");
        log.info("bearerToken {}", bearerToken);
        String kakaoAccessToken = parseBearerToken(bearerToken);
        log.info("kakaoAccessToken {}", kakaoAccessToken);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenValidationRequest =
                new HttpEntity<>(headers);

        RestTemplate rt = new RestTemplate();


        if (kakaoAccessToken != null) {
            headers.add("Authorization", "Bearer " + kakaoAccessToken);
            ResponseEntity<String> kakaoTokenValidationResponse = rt.exchange(
                    TOKEN_VALIDATION_URL,
                    HttpMethod.GET,
                    kakaoTokenValidationRequest,
                    String.class
            );
            if (kakaoTokenValidationResponse.getStatusCode() == HttpStatus.OK) {
                log.info("kakao token info {}", kakaoTokenValidationResponse.getBody());
                try {
                    Authentication authentication = jwtTokenUtil.getAuthenticationFromKakaoToken(kakaoAccessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.info("{} saved", authentication.getPrincipal());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                log.info("kakao token status code {}", kakaoTokenValidationResponse.getStatusCode());
            }
        }

//        if (StringUtils.hasText(jwtToken) && jwtTokenUtil.validateToken(jwtToken)) {
//            Authentication authentication = jwtTokenUtil.getAuthenticationFromToken(jwtToken);
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            log.info("{} saved", authentication.getPrincipal());
//        }
//        else {
//            log.info("{} requested, but no valid token provided.", httpServletRequest.getRequestURI());
//        }
        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
