package com.company.chatbot.service;

import com.company.chatbot.entity.Achievement;
import com.company.chatbot.entity.User;
import com.company.chatbot.entity.UserAchievement;
import com.company.chatbot.repository.AchievementRepository;
import com.company.chatbot.repository.UserAchievementRepository;
import com.company.chatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    private static final int POINTS_PER_MESSAGE = 10;
    private static final int POINTS_PER_LEVEL = 100;

    @Transactional
    public int awardPointsForMessage(User user) {
        int currentPoints = user.getTotalPoints();
        int newPoints = currentPoints + POINTS_PER_MESSAGE;
        
        user.setTotalPoints(newPoints);
        
        // Check for level up
        int currentLevel = user.getLevel();
        int newLevel = calculateLevel(newPoints);
        
        if (newLevel > currentLevel) {
            user.setLevel(newLevel);
            log.info("User {} leveled up to level {}", user.getUsername(), newLevel);
        }
        
        userRepository.save(user);
        return POINTS_PER_MESSAGE;
    }

    private int calculateLevel(int totalPoints) {
        return (totalPoints / POINTS_PER_LEVEL) + 1;
    }

    @Transactional
    public void checkAndAwardAchievements(User user) {
        List<Achievement> allAchievements = achievementRepository.findAll();
        
        for (Achievement achievement : allAchievements) {
            UserAchievement userAchievement = userAchievementRepository
                    .findByUserAndAchievement(user, achievement)
                    .orElseGet(() -> {
                        UserAchievement newUA = new UserAchievement();
                        newUA.setUser(user);
                        newUA.setAchievement(achievement);
                        newUA.setProgress(0);
                        newUA.setCompleted(false);
                        return newUA;
                    });

            if (!userAchievement.getCompleted()) {
                int currentProgress = calculateProgress(user, achievement);
                userAchievement.setProgress(currentProgress);
                
                if (currentProgress >= achievement.getTargetValue()) {
                    userAchievement.setCompleted(true);
                    
                    // Award achievement points
                    user.setTotalPoints(user.getTotalPoints() + achievement.getPointsReward());
                    userRepository.save(user);
                    
                    log.info("User {} earned achievement: {}", user.getUsername(), achievement.getName());
                }
                
                userAchievementRepository.save(userAchievement);
            }
        }
    }

    private int calculateProgress(User user, Achievement achievement) {
        return switch (achievement.getType()) {
            case MESSAGES_SENT -> user.getMessagesSent();
            case POINTS_EARNED -> user.getTotalPoints();
            case LEVEL_REACHED -> user.getLevel();
            case CONSECUTIVE_DAYS -> 0; // TODO: Implement login streak tracking
            case TOPICS_EXPLORED -> 0; // TODO: Implement topic tracking
        };
    }

    public List<UserAchievement> getUserAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userAchievementRepository.findByUser(user);
    }

    public List<UserAchievement> getCompletedAchievements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userAchievementRepository.findByUserAndCompleted(user, true);
    }
}
