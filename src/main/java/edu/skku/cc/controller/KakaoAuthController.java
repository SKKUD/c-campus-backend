package edu.skku.cc.controller;

import edu.skku.cc.jwt.dto.JwtDto;
import edu.skku.cc.service.KakaoAuthService;
import edu.skku.cc.service.dto.KakaoUserInfoDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

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