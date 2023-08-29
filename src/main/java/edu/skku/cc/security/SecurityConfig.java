package edu.skku.cc.security;

import edu.skku.cc.jwt.KakaoAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final KakaoAuthenticationFilter kakaoAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAuthorizationManager customAuthorizationManager;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.disable())
                .cors(c ->
                        c.configurationSource(corsConfigurationSource()
                        ))
                .httpBasic(AbstractHttpConfigurer::disable
                )
                .formLogin(login ->
                        login
                                .disable())
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
//                                .requestMatchers("/oauth2/kakao/logout").access(customAuthorizationManager)
                                .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
//                .logout(
//                        l -> l.logoutUrl("oauth2/kakao/logout")
//                                .logoutSuccessUrl("https://congcampus.com")
//                                .deleteCookies("accessToken")
//                                .deleteCookies("refreshToken")
//                                .permitAll()
//                )
                .exceptionHandling(exception ->
                        exception.
                                authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .addFilterBefore(
                        kakaoAuthenticationFilter, UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
        ));
        corsConfiguration.setAllowedHeaders(List.of(
                "*"
        ));
        corsConfiguration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://congcampus.com"
        ));
        corsConfiguration.setExposedHeaders(List.of(
                "*"
        ));

        corsConfiguration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
