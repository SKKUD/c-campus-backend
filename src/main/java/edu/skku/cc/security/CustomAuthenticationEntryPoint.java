package edu.skku.cc.security;

import edu.skku.cc.exception.CustomException;
import edu.skku.cc.exception.ErrorType;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof BadCredentialsException) {
            log.info("authException {}", authException.getClass());
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        } else if (authException instanceof DisabledException) {
            log.info("authException {}", authException.getClass());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden: Account is disabled");
        }
        else if (authException instanceof InsufficientAuthenticationException) {
            log.info("authException {}", authException.getClass());
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        } else {
            log.info("authException {}", authException.getClass());
            throw new CustomException(ErrorType.UNAUTHORIZED_USER_EXCEPTION);
        }
    }
}
