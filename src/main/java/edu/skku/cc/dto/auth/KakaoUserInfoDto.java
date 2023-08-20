package edu.skku.cc.dto.auth;

import edu.skku.cc.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KakaoUserInfoDto {
    private String nickname;
    private Long kakaoId;
    private String profileImageUrl;

    public User toEntity() {
        return new User(nickname, kakaoId, profileImageUrl);
    }
}
