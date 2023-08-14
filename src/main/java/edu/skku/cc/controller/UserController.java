package edu.skku.cc.controller;

import edu.skku.cc.domain.User;
import edu.skku.cc.dto.kakaoUser.UserResponseDto;
import edu.skku.cc.service.KakaoAuthService;
import edu.skku.cc.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import software.amazon.awssdk.http.HttpStatusCode;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final KakaoAuthService kakaoAuthService;
    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUserInfo(@PathVariable Long userId) {
        log.info("userId {}", userId);
        User user = userService.getUser(userId);
        if (user != null) {
            UserResponseDto userResponseDto = new UserResponseDto(user.getId(), user.getName());
            return ResponseEntity.status(HttpStatusCode.OK).body(userResponseDto);
        }
        return ResponseEntity.status(HttpStatusCode.BAD_REQUEST).body(null);
    }
}
