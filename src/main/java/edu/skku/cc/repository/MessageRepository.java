package edu.skku.cc.repository;

import edu.skku.cc.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.id = :id AND m.isPulled = TRUE ORDER BY m.pulledAt DESC")
    List<Message> findPulledByUserIdOrderByPulledAtDECS(Long id);
}
