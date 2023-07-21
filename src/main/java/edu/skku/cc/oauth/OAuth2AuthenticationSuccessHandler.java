package edu.skku.cc.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;

    public OAuth2AuthenticationSuccessHandler(CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository) {
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("SUCCESS!");
    }
}
