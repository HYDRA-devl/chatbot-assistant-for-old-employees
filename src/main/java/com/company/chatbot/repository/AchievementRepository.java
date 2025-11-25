package com.company.chatbot.repository;

import com.company.chatbot.entity.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByName(String name);
}
