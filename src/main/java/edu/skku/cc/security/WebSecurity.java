package edu.skku.cc.security;

import edu.skku.cc.domain.Message;
import edu.skku.cc.domain.User;
import edu.skku.cc.repository.MessageRepository;
import edu.skku.cc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("webSecurity")
@RequiredArgsConstructor
@Slf4j
public class WebSecurity {
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    public boolean checkAuthority(Authentication authentication, String userId) {
        log.info("userId {}", userId);
        User user = userRepository.findByEmail(String.valueOf(authentication.getPrincipal()));
        if (user != null && userId.equals(String.valueOf(user.getId()))) {
            log.info("user id {}", authentication.getCredentials());
            log.info("user authenticated {}", true);
            return true;
        }
        else {
            log.info("user authenticated {}", false);
            return false;
        }
    }

    public boolean checkAuthority(Authentication authentication, String userId, Long messageId) {
        Optional<Message> optionalMessage = messageRepository.findById(messageId);
        if (optionalMessage.isPresent()) {
            Message message = optionalMessage.get();
            log.info("message {}", message.getId());
            if (message.getIsPublic() == false) { // 위임
                log.info("message public or private {}", "private");
                return checkAuthority(authentication, userId);
            }
            log.info("message public or private {}", "public");
        }
        return true;
    }
}
