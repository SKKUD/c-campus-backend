package edu.skku.cc.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.skku.cc.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.LinkedHashMap;

//expiration
//invalid token
//
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;
    private final String TOKEN_VALIDATION_URL = "https://kapi.kakao.com/v1/user/access_token_info";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeaderValue = request.getHeader("Authorization");
        String bearerToken = null;
        String accessToken = null;
        log.info("authorizationHeaderValue {}", authorizationHeaderValue);
        if (authorizationHeaderValue != null) {
            bearerToken = authorizationHeaderValue;
            accessToken = parseBearerToken(bearerToken);
        }

        log.info("accessToken value: {}", accessToken);

        if (StringUtils.hasText(accessToken) && jwtTokenUtil.validateToken(accessToken)) {
            Authentication authentication = jwtTokenUtil.getAuthenticationFromToken(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("{} saved", authentication.getPrincipal());
        } else {
            SecurityContextHolder.clearContext();
        }
        try {
            filterChain.doFilter(request, response);
            log.info("Filter chain success");
        } catch (CustomException e) {
            String jsonString = createErrorResponse(e);
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(jsonString);
            if (jsonString == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("유효하지 않은 json 형식입니다.");
            }
        }
    }

    private String createErrorResponse(CustomException customException) {
        ObjectMapper objectMapper = new ObjectMapper();
        LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<>();
        jsonMap.put("status", customException.getHttpStatus());
        jsonMap.put("message", customException.getMessage());
        try {
            return objectMapper.writeValueAsString(jsonMap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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