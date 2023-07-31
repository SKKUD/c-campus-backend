package edu.skku.cc.security;

import edu.skku.cc.domain.User;
import edu.skku.cc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("webSecurity")
@RequiredArgsConstructor
@Slf4j
public class WebSecurity {
    private final UserRepository userRepository;
    public boolean checkAuthority(Authentication authentication, String userId) {
        User user = userRepository.findByEmail(String.valueOf(authentication.getPrincipal()));
        if (user != null && userId.equals(String.valueOf(user.getId()))) {
            return true;
        }
        else {
            log.info("userId {}", user.getId());
            return false;
        }
    }
}
