package edu.skku.cc.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KakaoTokenDto {
    private String accessToken;
    private String refreshToken;
}
