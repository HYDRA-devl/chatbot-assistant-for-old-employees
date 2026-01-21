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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificationService {

    private static final int POINTS_PER_MESSAGE = 10;
    private static final int POINTS_PER_LEVEL = 100;
    private static final int POINTS_PER_TASK = 5;
    private static final int POINTS_PER_MEETING = 15;

    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

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

    public int awardPointsForMessage(User user) {
        int currentPoints = user.getTotalPoints();
        int newPoints = currentPoints + POINTS_PER_MESSAGE;
        user.setTotalPoints(newPoints);

        int newLevel = calculateLevel(newPoints);
        user.setLevel(newLevel);

        userRepository.save(user);

        return POINTS_PER_MESSAGE;
    }

    public int awardPointsForActivity(User user, int completedTasks, int completedMeetings) {
        int pointsEarned = (completedTasks * POINTS_PER_TASK) + (completedMeetings * POINTS_PER_MEETING);
        if (pointsEarned <= 0) {
            return 0;
        }

        int newPoints = user.getTotalPoints() + pointsEarned;
        user.setTotalPoints(newPoints);
        user.setLevel(calculateLevel(newPoints));
        userRepository.save(user);
        return pointsEarned;
    }

    public void checkAndAwardAchievements(User user) {
        List<Achievement> allAchievements = achievementRepository.findAll();

        for (Achievement achievement : allAchievements) {
            if (userAchievementRepository.existsByUserAndAchievement(user, achievement)) {
                continue;
            }

            if (hasAchieved(user, achievement)) {
                UserAchievement userAchievement = new UserAchievement();
                userAchievement.setUser(user);
                userAchievement.setAchievement(achievement);
                userAchievement.setCompleted(true);
                userAchievement.setEarnedAt(LocalDateTime.now());

                userAchievementRepository.save(userAchievement);

                user.setTotalPoints(user.getTotalPoints() + achievement.getPointsReward());
                userRepository.save(user);
            }
        }
    }

    private boolean hasAchieved(User user, Achievement achievement) {
        switch (achievement.getType()) {
            case POINTS_EARNED -> {
                return user.getTotalPoints() >= achievement.getTargetValue();
            }
            default -> {
                return false;
            }
        }
    }

    private int calculateLevel(int totalPoints) {
        return (totalPoints / POINTS_PER_LEVEL) + 1;
    }
}
