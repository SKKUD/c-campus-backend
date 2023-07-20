package edu.skku.cc.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;

@Component
public class CookieAuthorizationRequestRepository implements AuthorizationRequestRepository {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)) {
                    String serializedAuthorizationRequest = cookie.getName();
                    return deserialize(serializedAuthorizationRequest);
                }
            }
        }
        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            deleteCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }
        addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, authorizationRequest);
        String redirectUri = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);

        if (!redirectUri.isBlank())
            addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    private static String serialize(Object object) {
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
    }

    private static OAuth2AuthorizationRequest deserialize(String serializedAuthorizationRequest) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(serializedAuthorizationRequest, OAuth2AuthorizationRequest.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void addCookie(HttpServletResponse response, String name, Object object) {
        Cookie cookie = new Cookie(name, serialize(object));
        cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
