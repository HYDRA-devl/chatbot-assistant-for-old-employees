package com.company.chatbot.controller;

import com.company.chatbot.dto.GoogleCalendarEventDto;
import com.company.chatbot.service.GoogleCalendarService;
import com.google.api.services.calendar.model.CalendarListEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/google-calendar")
@RequiredArgsConstructor
public class GoogleCalendarController {

    private final GoogleCalendarService googleCalendarService;

    @GetMapping("/calendars")
    public ResponseEntity<List<CalendarListEntry>> getCalendars() {
        return ResponseEntity.ok(googleCalendarService.listCalendars());
    }

    @GetMapping("/events")
    public ResponseEntity<List<GoogleCalendarEventDto>> getEvents(
        @RequestParam(required = false) String calendarId,
        @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(googleCalendarService.listEvents(calendarId, limit));
    }
}
