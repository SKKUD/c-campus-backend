package edu.skku.cc.service;

import edu.skku.cc.domain.User;
import edu.skku.cc.jwt.JwtTokenUtil;
import edu.skku.cc.jwt.dto.JwtDto;
import edu.skku.cc.redis.RedisUtil;
import edu.skku.cc.repository.UserRepository;
import edu.skku.cc.service.dto.KakaoUserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisUtil redisUtil;

    @Value("${spring.security.oauth2.client.provider.kakao.user-info-uri}")
    private String USER_INFO_URL;
    @Value("${spring.security.oauth2.client.provider.kakao.token-uri}")
    private String TOKEN_URL;
    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String CLIENT_ID;
    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String CLIENT_SECRET;
    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String REDIRECT_URI;

    public JwtDto kakaoLogin(String code) throws Exception {
        String kakaoAccessToken = kakaoAuthenticate(code);
        getKakaoUserInfo(kakaoAccessToken);
        JwtDto jwtDto = getJwtToken(kakaoAccessToken);
        return jwtDto;
    }

    private String kakaoAuthenticate(String code) throws Exception {
        String contentType = "application/x-www-form-urlencoded;charset=utf-8";
        String grantType = "authorization_code";

        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        // Headers
        headers.add("Content-type", contentType);

        // Request Body
        params.add("grant_type", grantType);
        params.add("client_id", CLIENT_ID);
        params.add("client_secret", CLIENT_SECRET);
        params.add("redirect_uri", REDIRECT_URI);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest =
                new HttpEntity<>(params, headers);

        RestTemplate rt = new RestTemplate();
        ResponseEntity<String> response = rt.exchange(
                TOKEN_URL,
                HttpMethod.POST,
                kakaoTokenRequest,
                String.class
        );

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
        String kakaoAccessToken = String.valueOf(jsonObject.get("access_token"));
//        String kakaoRefreshToken = String.valueOf(jsonObject.get("refresh_token"));

        return kakaoAccessToken;
    }

    private JwtDto getJwtToken(String kakaoAccessToken) throws Exception {
        KakaoUserInfoDto kakaoUserInfo = getKakaoUserInfo(kakaoAccessToken);
        User user = synchronizeUser(kakaoUserInfo);
        return getAccessTokenAndRefreshToken(user);
    }
    public KakaoUserInfoDto getKakaoUserInfo(String kakaoAccessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoAccessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        RestTemplate rt = new RestTemplate();
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = rt.exchange(USER_INFO_URL, HttpMethod.POST, httpEntity, String.class);

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
        log.info("jsonObject {}", jsonObject);
        JSONObject account = (JSONObject) jsonObject.get("kakao_account");
        JSONObject profile = (JSONObject) account.get("profile");

        String nickname = String.valueOf(profile.get("nickname"));
        String email = String.valueOf(account.get("email"));

        return new KakaoUserInfoDto(nickname, email);
    }

    private User synchronizeUser(KakaoUserInfoDto kakaoUserInfoDto) {
        User user = kakaoUserInfoDto.toEntity();
        User findUser = userRepository.findByEmail(user.getEmail());
        if (findUser == null)
            userRepository.save(user);
        return findUser;
    }

    private JwtDto getAccessTokenAndRefreshToken(User user) {
        log.info("user {}", user.getEmail());
        String key = user.getEmail();
        String accessToken = jwtTokenUtil.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenUtil.createRefreshToken(user.getEmail());
        redisUtil.saveRefreshToken(key, refreshToken, jwtTokenUtil.getRefreshTokenExpireTime(), TimeUnit.MILLISECONDS);
        return new JwtDto(accessToken, refreshToken);
    }
}
