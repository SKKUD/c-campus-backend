package edu.skku.cc.security;

import edu.skku.cc.domain.Message;
import edu.skku.cc.domain.User;
import edu.skku.cc.repository.MessageRepository;
import edu.skku.cc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

@Component("webSecurity")
@RequiredArgsConstructor
@Slf4j
public class WebSecurity {
    private final UserRepository userRepository;
    public boolean checkAuthority(Authentication authentication, UUID userId) {
        log.info("userId {}", userId);
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent() && optionalUser.get().getId() == userId) {
            log.info("user id {}", authentication.getCredentials());
            log.info("user authenticated {}", true);
            return true;
        }
        else {
            log.info("user authenticated {}", false);
            return false;
        }
    }
}
