package edu.skku.cc.repository;

import edu.skku.cc.domain.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    Optional<Photo> findByImageUuid(UUID imageUuid);
}
