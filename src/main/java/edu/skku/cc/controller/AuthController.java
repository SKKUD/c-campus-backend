package edu.skku.cc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @GetMapping("/oauth2/callback/kakao")
    public String kakaoCallback() {
//        OAuth2AuthorizedClient authorizedClient = this.authorizedClientService.loadAuthorizedClient("kakao", authentication.getName());
//        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return "";
    }
}
