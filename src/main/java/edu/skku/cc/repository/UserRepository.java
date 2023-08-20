package edu.skku.cc.repository;

import edu.skku.cc.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByKakaoId(Long kakaoId);
}
