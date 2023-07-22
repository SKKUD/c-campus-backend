package edu.skku.cc.security;

import edu.skku.cc.oauth.CookieAuthorizationRequestRepository;
import edu.skku.cc.oauth.CustomOAuth2UserService;
import edu.skku.cc.oauth.OAuth2AuthenticationFailureHandler;
import edu.skku.cc.oauth.OAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

import java.util.function.Consumer;

//@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class OAuth2Config {

    private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    public OAuth2Config(CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository, CustomOAuth2UserService customOAuth2UserService, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler, OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler) {
        this.cookieAuthorizationRequestRepository = cookieAuthorizationRequestRepository;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
//                .httpBasic(hb ->
//                        hb
//                                .disable()
//                )
                .formLogin(login ->
                        login
                                .disable())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .anyRequest().permitAll()
                );
//                .sessionManagement(session ->
//                        session
//                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .logout(logout ->
//                        logout
//                                .disable()
//                );
//                .addFilterBefore()
//                .oauth2Login(oauth2 -> oauth2
//                        .authorizationEndpoint(endPoint -> endPoint
//                                .baseUri("/oauth2/authorize")
//                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
//                        )
//                        .redirectionEndpoint(endPoint -> endPoint
//                                .baseUri("/oauth2/callback/*")
//                        )
//                        .userInfoEndpoint(endPoint -> endPoint
//                                .userService(customOAuth2UserService)
//                        )
//                        .successHandler(oAuth2AuthenticationSuccessHandler)
//                        .failureHandler(oAuth2AuthenticationFailureHandler)
                return http.build();
    }
}
