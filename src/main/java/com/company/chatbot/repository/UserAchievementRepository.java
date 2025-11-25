package com.company.chatbot.repository;

import com.company.chatbot.entity.Achievement;
import com.company.chatbot.entity.User;
import com.company.chatbot.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUser(User user);
    List<UserAchievement> findByUserAndCompleted(User user, Boolean completed);
    Optional<UserAchievement> findByUserAndAchievement(User user, Achievement achievement);
}
