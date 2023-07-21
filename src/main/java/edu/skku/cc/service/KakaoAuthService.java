package edu.skku.cc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
public class KakaoAuthService {

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
    private String CONTENT_TYPE = "application/x-www-form-urlencoded;charset=utf-8";
    private String GRANT_TYPE = "authorization_code";

    public void getKakaoUserInfo(String code) {
        try {
            HttpHeaders headers = new HttpHeaders();
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

            // Headers
            headers.add("Content-type", CONTENT_TYPE);

            // Request Body
            params.add("grant_type", GRANT_TYPE);
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
            String accessToken = (String) jsonObject.get("access_token");
            String refreshToken = (String) jsonObject.get("refresh_token");

            log.info("accessToken {}", accessToken);
            log.info("refreshToken {}", refreshToken);

            getKakaoUserInfoByToken(accessToken);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void getKakaoUserInfoByToken (String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

            RestTemplate rt = new RestTemplate();
            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = rt.exchange(USER_INFO_URL, HttpMethod.POST, httpEntity, String.class);

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
            System.out.println(jsonObject.toJSONString());
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
