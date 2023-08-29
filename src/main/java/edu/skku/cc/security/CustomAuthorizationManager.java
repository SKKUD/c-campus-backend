package edu.skku.cc.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.function.Supplier;

@Slf4j
@Component
public class CustomAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        AuthorizationManager.super.verify(authentication, object);
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        Object authPrincipal = authentication.get().getPrincipal();
        String authorizationHeaderValue = object.getRequest().getHeader("Authorization");
        log.info("[authorization manager]");
        log.info("authPrincipal {}", authPrincipal);
        log.info("authorizationHeader {}", authorizationHeaderValue);
        if (authPrincipal != null && String.valueOf(authPrincipal).equals(authorizationHeaderValue)) {
            log.info("SUCCESS!");
        } else {
            log.info("FAIL!");
        }
        return null;
    }
}