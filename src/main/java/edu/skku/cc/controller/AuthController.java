package edu.skku.cc.controller;

import edu.skku.cc.dto.auth.AuthenticationResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import software.amazon.awssdk.http.HttpStatusCode;

@Controller
public class AuthController {
    @GetMapping("/auth/authentication")
    public ResponseEntity<AuthenticationResponseDto> checkAuthentication(HttpServletRequest request, Authentication authentication) {
        HttpSession session = request.getSession(false);
        if (session != null && authentication != null) {
            String userId = String.valueOf(authentication.getPrincipal());
            return ResponseEntity.status(HttpStatusCode.OK).body(new AuthenticationResponseDto(Long.parseLong(userId)));
        } else {
            return ResponseEntity.status(HttpStatusCode.UNAUTHORIZED).body(new AuthenticationResponseDto(null));
        }
    }
}
