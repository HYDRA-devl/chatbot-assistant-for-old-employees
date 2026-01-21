package com.company.chatbot.dto;

public record ActivitySyncResult(
    int tasksCompletedAdded,
    int meetingsCompletedAdded,
    int pointsEarned,
    int totalTasksCompleted,
    int totalMeetingsCompleted,
    int totalPoints
) {}
