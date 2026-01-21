package com.company.chatbot.dto;

import java.util.List;

public record CompletedActivityResponse(
    List<String> completedTaskIds,
    List<String> completedMeetingIds
) {}
