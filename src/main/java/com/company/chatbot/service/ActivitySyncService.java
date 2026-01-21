package com.company.chatbot.service;

import com.company.chatbot.dto.ActivitySyncResult;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivitySyncService {

    private final UserRepository userRepository;
    private final GoogleTasksService googleTasksService;
    private final GoogleCalendarService googleCalendarService;
    private final GamificationService gamificationService;

    public ActivitySyncResult syncUserActivity(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime lastSync = user.getLastActivitySyncAt();
        if (lastSync == null) {
            lastSync = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now().minusDays(30);
        }

        Instant since = lastSync.atZone(ZoneId.systemDefault()).toInstant();

        int tasksCompleted = googleTasksService.countCompletedTasksSince(since);
        int meetingsCompleted = googleCalendarService.countCompletedMeetingsSince(since);

        user.setTasksCompleted((user.getTasksCompleted() == null ? 0 : user.getTasksCompleted()) + tasksCompleted);
        user.setMeetingsCompleted((user.getMeetingsCompleted() == null ? 0 : user.getMeetingsCompleted()) + meetingsCompleted);

        int pointsEarned = gamificationService.awardPointsForActivity(user, tasksCompleted, meetingsCompleted);
        user.setLastActivitySyncAt(LocalDateTime.now());

        userRepository.save(user);
        gamificationService.checkAndAwardAchievements(user);

        return new ActivitySyncResult(
            tasksCompleted,
            meetingsCompleted,
            pointsEarned,
            user.getTasksCompleted(),
            user.getMeetingsCompleted(),
            user.getTotalPoints()
        );
    }
}
