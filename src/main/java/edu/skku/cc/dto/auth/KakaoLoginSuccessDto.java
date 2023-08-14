package edu.skku.cc.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KakaoLoginSuccessDto {
    private Long userId;
    private String kakaoAccessToken;
}
