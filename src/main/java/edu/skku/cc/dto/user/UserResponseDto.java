package edu.skku.cc.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private UUID userId;
    private String nickname;
    private String profileImageUrl;
}