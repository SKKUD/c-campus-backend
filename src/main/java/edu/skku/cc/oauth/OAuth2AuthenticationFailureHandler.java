package edu.skku.cc.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;

    public OAuth2AuthenticationFailureHandler(CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository) {
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        String targetUrl = getCookie(request, "redirect_uri")
                .map(Cookie::getValue)
                .orElse("/");

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        System.out.println(targetUrl);
    }

    private Optional<Cookie> getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }
}
