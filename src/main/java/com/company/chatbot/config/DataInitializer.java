package com.company.chatbot.config;

import com.company.chatbot.entity.Achievement;
import com.company.chatbot.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AchievementRepository achievementRepository;

    @Override
    public void run(String... args) {
        if (achievementRepository.count() == 0) {
            log.info("Initializing default achievements...");

            createAchievement("First Steps", "Send your first message", "🌟", 10, 
                    Achievement.AchievementType.MESSAGES_SENT, 1);
            
            createAchievement("Conversationalist", "Send 50 messages", "💬", 50, 
                    Achievement.AchievementType.MESSAGES_SENT, 50);
            
            createAchievement("Expert Learner", "Send 100 messages", "🎓", 100, 
                    Achievement.AchievementType.MESSAGES_SENT, 100);
            
            createAchievement("Point Master", "Earn 500 points", "⭐", 100, 
                    Achievement.AchievementType.POINTS_EARNED, 500);
            
            createAchievement("Rising Star", "Reach level 5", "🚀", 150, 
                    Achievement.AchievementType.LEVEL_REACHED, 5);
            
            createAchievement("Tech Guru", "Reach level 10", "👨‍💻", 300, 
                    Achievement.AchievementType.LEVEL_REACHED, 10);

            log.info("Default achievements created successfully!");
        }
    }

    private void createAchievement(String name, String description, String icon, 
                                   int pointsReward, Achievement.AchievementType type, int targetValue) {
        Achievement achievement = new Achievement();
        achievement.setName(name);
        achievement.setDescription(description);
        achievement.setIconUrl(icon);
        achievement.setPointsReward(pointsReward);
        achievement.setType(type);
        achievement.setTargetValue(targetValue);
        achievementRepository.save(achievement);
    }
}
