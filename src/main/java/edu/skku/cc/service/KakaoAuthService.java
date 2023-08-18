package edu.skku.cc.service;

import edu.skku.cc.domain.User;
import edu.skku.cc.dto.auth.KakaoUserInfoDto;
import edu.skku.cc.dto.jwt.KakaoLoginSuccessDto;
import edu.skku.cc.jwt.JwtTokenUtil;
import edu.skku.cc.jwt.dto.KakaoAccessTokenDto;
import edu.skku.cc.redis.RedisUtil;
import edu.skku.cc.repository.UserRepository;
import edu.skku.cc.service.dto.KakaoTokenDto;
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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class KakaoAuthService {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final RedisUtil redisUtil;
    private final S3Client s3Client;


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
    @Value("${aws.s3.bucket}")
    private String BUCKET_NAME;

    @Value("${aws.s3.region}")
    private String REGION;

    public KakaoLoginSuccessDto kakaoLogin(String code) throws Exception {
        KakaoTokenDto kakaoTokenDto = kakaoAuthenticate(code);
        User user = saveKakaoUserInfo(kakaoTokenDto);
        KakaoLoginSuccessDto kakaoLoginSuccessDto = getAccessTokenAndRefreshToken(user);
        return kakaoLoginSuccessDto;
    }

    public ResponseEntity<String> kakaoLogout() {
        RestTemplate rt = new RestTemplate();

        String logoutRedirectUrl = "http://localhost:3000";
        String kakaoLogoutUrl = "https://kauth.kakao.com/oauth/logout?client_id=" + CLIENT_ID + "&logout_redirect_uri=" + logoutRedirectUrl;

        ResponseEntity<String> responseEntity = rt.getForEntity(kakaoLogoutUrl, String.class);
        return responseEntity;
    }

    private KakaoTokenDto kakaoAuthenticate(String code) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

        String contentType = "application/x-www-form-urlencoded;charset=utf-8";
        String grantType = "authorization_code";

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
        String kakaoRefreshToken = String.valueOf(jsonObject.get("refresh_token"));

        KakaoTokenDto kakaoTokenDto = new KakaoTokenDto(kakaoAccessToken, kakaoRefreshToken);

        log.info("kakaoTokenDto {}", kakaoTokenDto);

        return kakaoTokenDto;
    }

    public User saveKakaoUserInfo(KakaoTokenDto kakaoTokenDto) throws Exception {
        KakaoUserInfoDto kakaoUserInfoDto = getKakaoUserInfo(kakaoTokenDto);
        User user = kakaoUserInfoDto.toEntity();
        User findUser = userRepository.findByEmail(user.getEmail());
        if (findUser == null) {
            return userRepository.save(user);
        }
        return userRepository.save(findUser);
    }

    private KakaoUserInfoDto getKakaoUserInfo(KakaoTokenDto kakaoTokenDto) throws Exception {
        RestTemplate rt = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        JSONParser jsonParser = new JSONParser();

        headers.add("Authorization", "Bearer " + kakaoTokenDto.getAccessToken());
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = rt.exchange(
                USER_INFO_URL,
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody());
        JSONObject account = (JSONObject) jsonObject.get("kakao_account");
        JSONObject profile = (JSONObject) account.get("profile");

        log.info("profile {}", profile);

        String nickname = String.valueOf(profile.get("nickname"));
        String profileImageUrl = String.valueOf(profile.get("profile_image_url"));
        String email = String.valueOf(account.get("email"));

        log.info("nickname {}", nickname);
        log.info("email {}", email);
        log.info("profile_image_url {}", profileImageUrl);

        URL url = new URL(profileImageUrl);
        InputStream inputStream = url.openStream();

        UUID uuid = UUID.randomUUID();
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(uuid.toString())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, url.openConnection().getContentLength()));
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            s3Client.close();
        }
        return new KakaoUserInfoDto(nickname, email, uuid);
    }

    public String getNewAccessToken(String refreshToken) throws Exception{
        String userId = redisUtil.validateRefreshToken(refreshToken);
        log.info("refreshToken userId {}", userId);
        if (userId == null) {
            return null;
        }
        String accessToken = jwtTokenUtil.createAccessToken(userId);
        return accessToken;
    }

    private KakaoLoginSuccessDto getAccessTokenAndRefreshToken(User user) {
        log.info("user {}", user.getId());
        String userId = String.valueOf(user.getId());
        String accessToken = jwtTokenUtil.createAccessToken(userId);
        String refreshToken = jwtTokenUtil.createRefreshToken();
        log.info("userId {}", userId);
        log.info("refreshToken {}", refreshToken);
        redisUtil.saveRefreshToken(refreshToken, userId, jwtTokenUtil.getRefreshTokenExpireTime(), TimeUnit.MILLISECONDS);
        return new KakaoLoginSuccessDto(user.getId(), accessToken, refreshToken);
    }
}
