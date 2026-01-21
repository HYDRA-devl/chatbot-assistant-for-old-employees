package com.company.chatbot.dto;

public record CompleteMeetingRequest(
    Long userId,
    String eventId,
    String summary
) {}
