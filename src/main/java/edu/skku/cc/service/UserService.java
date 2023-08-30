package edu.skku.cc.service;

import edu.skku.cc.domain.User;
import edu.skku.cc.dto.user.UserDto;
import edu.skku.cc.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    @Value("${aws.s3.bucket}")
    private String BUCKET_NAME;

    @Value("${aws.s3.region}")
    private String REGION;
    public UserDto getUser(UUID userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String profileImageUrl = "https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/" + user.getProfileImageUuid();
            return new UserDto(user.getId(), user.getName(), profileImageUrl);
        }
        else
            return null;
    }
}
