package com.company.chatbot.dto;

import java.util.List;

public record GoogleCalendarEventDto(
    String id,
    String summary,
    String start,
    String end,
    String location,
    List<String> attendees,
    String meetingLink,
    String htmlLink
) {}
