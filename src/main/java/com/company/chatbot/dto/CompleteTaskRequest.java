package com.company.chatbot.dto;

public record CompleteTaskRequest(
    Long userId,
    String taskId,
    String taskListId,
    String title
) {}
