package edu.skku.cc.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("webSecurity")
public class WebSecurity {
    public boolean checkAuthority(Authentication authentication, String userId) {
        return authentication.getPrincipal().equals(userId);
    }
}
