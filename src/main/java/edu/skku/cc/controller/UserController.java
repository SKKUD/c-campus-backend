package edu.skku.cc.controller;

import edu.skku.cc.dto.user.UserDto;
import edu.skku.cc.dto.user.UserResponseDto;
import edu.skku.cc.service.KakaoAuthService;
import edu.skku.cc.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final KakaoAuthService kakaoAuthService;
    private final UserService userService;

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponseDto> getUserInfo(@PathVariable UUID userId) {
        log.info("userId {}", userId);
        UserDto userDto = userService.getUser(userId);
        if (userDto != null) {
            UserResponseDto userResponseDto = new UserResponseDto(userDto.getUserId(), userDto.getNickname(), userDto.getProfileImageUrl());
            return ResponseEntity.status(HttpStatusCode.OK).body(userResponseDto);
        }
        return ResponseEntity.status(HttpStatusCode.BAD_REQUEST).body(null);
    }
}
