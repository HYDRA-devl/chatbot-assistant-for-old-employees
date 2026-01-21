package com.company.chatbot.controller;

import com.company.chatbot.dto.ActivitySyncResult;
import com.company.chatbot.entity.User;
import com.company.chatbot.entity.UserAchievement;
import com.company.chatbot.repository.UserRepository;
import com.company.chatbot.service.ActivitySyncService;
import com.company.chatbot.service.GamificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;
    private final UserRepository userRepository;
    private final ActivitySyncService activitySyncService;

    @GetMapping("/users/{userId}/achievements")
    public ResponseEntity<List<UserAchievement>> getUserAchievements(@PathVariable Long userId) {
        List<UserAchievement> achievements = gamificationService.getUserAchievements(userId);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/users/{userId}/achievements/completed")
    public ResponseEntity<List<UserAchievement>> getCompletedAchievements(@PathVariable Long userId) {
        List<UserAchievement> achievements = gamificationService.getCompletedAchievements(userId);
        return ResponseEntity.ok(achievements);
    }

    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<User> getUserStats(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/{userId}/sync-activity")
    public ResponseEntity<ActivitySyncResult> syncUserActivity(@PathVariable Long userId) {
        return ResponseEntity.ok(activitySyncService.syncUserActivity(userId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<User>> getLeaderboard() {
        List<User> leaderboard = userRepository.findAll();
        leaderboard.sort((u1, u2) -> u2.getTotalPoints().compareTo(u1.getTotalPoints()));
        return ResponseEntity.ok(leaderboard);
    }
}
