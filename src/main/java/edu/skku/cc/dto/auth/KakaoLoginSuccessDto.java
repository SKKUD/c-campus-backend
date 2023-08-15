package edu.skku.cc.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KakaoLoginSuccessDto {
    private Long userId;
    private String accessToken;
    private String refreshToken;
}
