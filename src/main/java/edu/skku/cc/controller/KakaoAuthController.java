package edu.skku.cc.controller;

import edu.skku.cc.dto.jwt.JwtDto;
import edu.skku.cc.service.KakaoAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@RequiredArgsConstructor
@Controller
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/oauth2/callback/kakao")
    public @ResponseBody ResponseEntity<JwtDto> kakaoCallback(String code) throws Exception {
        JwtDto jwtDto = kakaoAuthService.kakaoLogin(code);
        return ResponseEntity.ok().body(jwtDto);
    }

    @GetMapping("/oauth/logout")
    public @ResponseBody ResponseEntity kakaoLogout() {
        return kakaoAuthService.kakaoLogout();
    }
}