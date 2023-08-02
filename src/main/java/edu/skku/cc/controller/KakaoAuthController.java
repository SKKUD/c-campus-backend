package edu.skku.cc.controller;

import edu.skku.cc.jwt.dto.JwtDto;
import edu.skku.cc.jwt.dto.KakaoAccessTokenDto;
import edu.skku.cc.service.KakaoAuthService;
import edu.skku.cc.service.dto.KakaoTokenDto;
import edu.skku.cc.service.dto.KakaoUserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Controller
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @GetMapping("/oauth2/callback/kakao")
    public @ResponseBody ResponseEntity<KakaoTokenDto> kakaoCallback(String code) throws Exception {
        KakaoTokenDto kakaoTokenDto = kakaoAuthService.kakaoLogin(code);
        return ResponseEntity.ok().body(kakaoTokenDto);
    }
    
    @PostMapping("/oauth2/kakao/logout")
    public @ResponseBody ResponseEntity kakaoLogout() {
        ResponseEntity re = kakaoAuthService.kakaoLogout();
        log.info("logout response {}", re);
        return re;
    }

    @PostMapping("/oauth2/kakao/refresh")
    public @ResponseBody ResponseEntity kakaoRefresh(@RequestParam("refresh_token") String refreshToken) throws Exception{
        try {
            KakaoAccessTokenDto newAccessToken = kakaoAuthService.getNewKakaoAccessToken(refreshToken);
            return ResponseEntity.ok().body(newAccessToken);
        } catch (HttpClientErrorException.BadRequest e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}