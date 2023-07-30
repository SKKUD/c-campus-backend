package edu.skku.cc.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String bearerToken = request.getHeader("Authorization");
        log.info("bearerToken {}", bearerToken);
        String jwtToken = parseBearerToken(bearerToken);
        log.info("jwtToken {}", jwtToken);
        if (StringUtils.hasText(jwtToken) && jwtTokenUtil.validateToken(jwtToken)) {
            Authentication authentication = jwtTokenUtil.getAuthenticationFromToken(jwtToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("{} saved", authentication.getPrincipal());
        } else {
            log.info("{} requested, but no valid token provided.", request.getRequestURI());
        }
        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(String bearerToken) {
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
