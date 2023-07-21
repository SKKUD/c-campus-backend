package edu.skku.cc.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.Base64;
import java.util.Optional;

@Component
public class CookieAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        System.out.println("HI");
        Optional<Cookie> cookie = getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        return cookie
                .map(c -> deserialize(c.getName()))
                .orElse(null);
    }


    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        System.out.println("HEY");
        if (authorizationRequest == null) {
            deleteCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            deleteCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME);
            return;
        }
        addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
        String redirectUri = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.isNotBlank(redirectUri)) {
            addCookie(response, REDIRECT_URI_PARAM_COOKIE_NAME, redirectUri, COOKIE_EXPIRE_SECONDS);
        }
    } // save -> remove //response 또는 redirect 문제

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        System.out.println("BYE");
        System.out.println(request.getCookies().length);
        System.out.println(getCookie(request, "oauth2_auth_request"));
        return loadAuthorizationRequest(request);
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

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        System.out.println(cookie.getName());
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setValue("");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
