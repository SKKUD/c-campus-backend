package edu.skku.cc.controller;

import edu.skku.cc.dto.auth.AuthenticationResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import software.amazon.awssdk.http.HttpStatusCode;

@Controller
public class AuthController {
    @GetMapping("/auth/authentication")
    public ResponseEntity<AuthenticationResponseDto> checkAuthentication(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return ResponseEntity.status(HttpStatusCode.OK).body(new AuthenticationResponseDto(true));
        } else {
            return ResponseEntity.status(HttpStatusCode.UNAUTHORIZED).body(new AuthenticationResponseDto(false));
        }
    }
}
