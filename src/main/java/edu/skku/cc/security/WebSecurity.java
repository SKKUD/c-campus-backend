package edu.skku.cc.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("webSecurity")
public class WebSecurity {
    public boolean checkAuthority(Authentication authentication, String userId) {
        if (authentication.getPrincipal().equals(userId))
            return true;
        else
            return false;
    }
}
