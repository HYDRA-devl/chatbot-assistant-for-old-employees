package com.company.chatbot.service;

import com.company.chatbot.dto.GoogleCalendarEventDto;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.EntryPoint;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Employee Chatbot";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private final GoogleAuthService googleAuthService;

    private volatile Calendar calendarClient;
    private final Object calendarClientLock = new Object();

    public List<CalendarListEntry> listCalendars() {
        try {
            Calendar client = getOrInitCalendarClient();
            CalendarList list = client.calendarList().list().execute();
            return list.getItems();
        } catch (Exception e) {
            log.error("Failed to fetch Google Calendars", e);
            throw new RuntimeException("Failed to fetch Google Calendars");
        }
    }

    public List<GoogleCalendarEventDto> listEvents(String calendarId, Integer maxResults) {
        try {
            Calendar client = getOrInitCalendarClient();
            String resolvedCalendarId = calendarId != null ? calendarId : "primary";
            Calendar.Events.List request = client.events().list(resolvedCalendarId)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .setTimeMin(new com.google.api.client.util.DateTime(Instant.now().toString()));
            if (maxResults != null) {
                request.setMaxResults(maxResults);
            }
            Events events = request.execute();
            return events.getItems().stream()
                .map(this::toDto)
                .toList();
        } catch (Exception e) {
            log.error("Failed to fetch Google Calendar events", e);
            throw new RuntimeException("Failed to fetch Google Calendar events");
        }
    }

    public int countCompletedMeetingsSince(Instant since) {
        try {
            Calendar client = getOrInitCalendarClient();
            String resolvedCalendarId = "primary";

            Instant now = Instant.now();
            Instant buffer = since.minusSeconds(7 * 24 * 60 * 60L);

            Calendar.Events.List request = client.events().list(resolvedCalendarId)
                .setSingleEvents(true)
                .setTimeMin(new DateTime(buffer.toEpochMilli()))
                .setTimeMax(new DateTime(now.toEpochMilli()))
                .setOrderBy("startTime");

            Events events = request.execute();
            if (events.getItems() == null) {
                return 0;
            }

            int completedCount = 0;
            for (Event event : events.getItems()) {
                if (event == null || "cancelled".equalsIgnoreCase(event.getStatus())) {
                    continue;
                }
                EventDateTime end = event.getEnd();
                if (end == null) {
                    continue;
                }
                DateTime endTime = end.getDateTime();
                if (endTime == null) {
                    endTime = end.getDate();
                }
                if (endTime == null) {
                    continue;
                }
                long endMillis = endTime.getValue();
                if (endMillis >= since.toEpochMilli() && endMillis <= now.toEpochMilli()) {
                    completedCount += 1;
                }
            }
            return completedCount;
        } catch (Exception e) {
            log.error("Failed to count completed Google Calendar meetings", e);
            throw new RuntimeException("Failed to count completed Google Calendar meetings");
        }
    }

    private GoogleCalendarEventDto toDto(Event event) {
        return new GoogleCalendarEventDto(
            event.getId(),
            event.getSummary(),
            formatEventDateTime(event.getStart()),
            formatEventDateTime(event.getEnd()),
            event.getLocation(),
            event.getAttendees() == null
                ? List.of()
                : event.getAttendees().stream()
                    .map(EventAttendee::getEmail)
                    .filter(Objects::nonNull)
                    .toList(),
            extractMeetingLink(event),
            event.getHtmlLink()
        );
    }

    private String formatEventDateTime(EventDateTime eventDateTime) {
        if (eventDateTime == null) {
            return null;
        }
        DateTime dateTime = eventDateTime.getDateTime();
        if (dateTime == null) {
            dateTime = eventDateTime.getDate();
        }
        return dateTime != null ? dateTime.toStringRfc3339() : null;
    }

    private String extractMeetingLink(Event event) {
        if (event == null) {
            return null;
        }
        if (event.getHangoutLink() != null) {
            return event.getHangoutLink();
        }
        ConferenceData conferenceData = event.getConferenceData();
        if (conferenceData == null || conferenceData.getEntryPoints() == null) {
            return null;
        }
        return conferenceData.getEntryPoints().stream()
            .filter(Objects::nonNull)
            .filter(entryPoint -> {
                String type = entryPoint.getEntryPointType();
                return "video".equalsIgnoreCase(type) || "more".equalsIgnoreCase(type);
            })
            .map(EntryPoint::getUri)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    private Calendar getOrInitCalendarClient() {
        if (calendarClient != null) {
            return calendarClient;
        }
        synchronized (calendarClientLock) {
            if (calendarClient != null) {
                return calendarClient;
            }
            try {
                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                calendarClient = new Calendar.Builder(httpTransport, JSON_FACTORY, googleAuthService.getCredential())
                    .setApplicationName(APPLICATION_NAME)
                    .build();
                return calendarClient;
            } catch (Exception e) {
                log.error("Failed to initialize Google Calendar client", e);
                throw new RuntimeException("Failed to initialize Google Calendar client");
            }
        }
    }
}
