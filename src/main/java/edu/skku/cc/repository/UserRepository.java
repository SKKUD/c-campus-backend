package edu.skku.cc.repository;

import edu.skku.cc.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByKakaoId(Long kakaoId);
}
