package edu.skku.cc.dto.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KakaoLoginSuccessDto {
    private UUID userId;
    private String accessToken;
    private String refreshToken;
}
