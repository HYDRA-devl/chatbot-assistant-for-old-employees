package com.company.chatbot.service;

import com.company.chatbot.dto.ActivitySyncResult;
import com.company.chatbot.dto.CompleteMeetingRequest;
import com.company.chatbot.dto.CompleteTaskRequest;
import com.company.chatbot.dto.CompletedActivityResponse;
import com.company.chatbot.entity.CompletedMeeting;
import com.company.chatbot.entity.CompletedTask;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.CompletedMeetingRepository;
import com.company.chatbot.repository.CompletedTaskRepository;
import com.company.chatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityCompletionService {

    private final UserRepository userRepository;
    private final CompletedTaskRepository completedTaskRepository;
    private final CompletedMeetingRepository completedMeetingRepository;
    private final GoogleTasksService googleTasksService;
    private final GamificationService gamificationService;

    public ActivitySyncResult completeTask(CompleteTaskRequest request) {
        if (request == null || request.userId() == null || request.taskId() == null) {
            throw new RuntimeException("Invalid task completion request");
        }

        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (completedTaskRepository.existsByUserAndTaskId(user, request.taskId())) {
            return new ActivitySyncResult(0, 0, 0,
                user.getTasksCompleted(), user.getMeetingsCompleted(), user.getTotalPoints());
        }

        if (request.taskListId() != null) {
            googleTasksService.markTaskCompleted(request.taskListId(), request.taskId());
        }

        CompletedTask completedTask = new CompletedTask();
        completedTask.setUser(user);
        completedTask.setTaskId(request.taskId());
        completedTask.setTaskTitle(request.title());
        completedTask.setCompletedAt(LocalDateTime.now());
        completedTaskRepository.save(completedTask);

        user.setTasksCompleted((user.getTasksCompleted() == null ? 0 : user.getTasksCompleted()) + 1);
        if (user.getMeetingsCompleted() == null) {
            user.setMeetingsCompleted(0);
        }
        int pointsEarned = gamificationService.awardPointsForActivity(user, 1, 0);

        return new ActivitySyncResult(1, 0, pointsEarned,
            user.getTasksCompleted(), user.getMeetingsCompleted(), user.getTotalPoints());
    }

    public ActivitySyncResult completeMeeting(CompleteMeetingRequest request) {
        if (request == null || request.userId() == null || request.eventId() == null) {
            throw new RuntimeException("Invalid meeting completion request");
        }

        User user = userRepository.findById(request.userId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (completedMeetingRepository.existsByUserAndEventId(user, request.eventId())) {
            return new ActivitySyncResult(0, 0, 0,
                user.getTasksCompleted(), user.getMeetingsCompleted(), user.getTotalPoints());
        }

        CompletedMeeting completedMeeting = new CompletedMeeting();
        completedMeeting.setUser(user);
        completedMeeting.setEventId(request.eventId());
        completedMeeting.setEventSummary(request.summary());
        completedMeeting.setCompletedAt(LocalDateTime.now());
        completedMeetingRepository.save(completedMeeting);

        user.setMeetingsCompleted((user.getMeetingsCompleted() == null ? 0 : user.getMeetingsCompleted()) + 1);
        if (user.getTasksCompleted() == null) {
            user.setTasksCompleted(0);
        }
        int pointsEarned = gamificationService.awardPointsForActivity(user, 0, 1);

        return new ActivitySyncResult(0, 1, pointsEarned,
            user.getTasksCompleted(), user.getMeetingsCompleted(), user.getTotalPoints());
    }

    public CompletedActivityResponse getCompletedActivities(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<String> tasks = completedTaskRepository.findByUser(user).stream()
            .map(CompletedTask::getTaskId)
            .toList();
        List<String> meetings = completedMeetingRepository.findByUser(user).stream()
            .map(CompletedMeeting::getEventId)
            .toList();

        return new CompletedActivityResponse(tasks, meetings);
    }
}
