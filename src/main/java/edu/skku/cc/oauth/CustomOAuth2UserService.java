package edu.skku.cc.oauth;

import edu.skku.cc.oauth.userinfo.KakaoUser;
import edu.skku.cc.oauth.userinfo.OAuth2UserInfo;
import edu.skku.cc.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService {

    private final UserRepository userRepository;
    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("HIBRO");
        System.out.println(userRequest.getAccessToken());
        OAuth2UserService oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);
        return process(userRequest, oAuth2User);
    }

    protected OAuth2User process(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        OAuth2UserInfo oAuth2UserInfo = new KakaoUser(oAuth2User.getAttributes());
        System.out.println("oAuth2UserInfo.getEmail() = " + oAuth2UserInfo.getEmail());
        return null;
    }
}
