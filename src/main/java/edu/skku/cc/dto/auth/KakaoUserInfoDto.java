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
    private String email;
    private String profileImageUrl;

    public User toEntity() {
        return new User(nickname, email, profileImageUrl);
    }
}
