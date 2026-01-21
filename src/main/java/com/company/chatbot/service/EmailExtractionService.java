package com.company.chatbot.service;

import com.company.chatbot.entity.EmailEntity;
import com.company.chatbot.entity.ExtractionJob;
import com.company.chatbot.entity.MeetingEntity;
import com.company.chatbot.entity.TaskEntity;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.EmailRepository;
import com.company.chatbot.repository.ExtractionJobRepository;
import com.company.chatbot.repository.MeetingRepository;
import com.company.chatbot.repository.TaskRepository;
import com.company.chatbot.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailExtractionService {

    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final TaskRepository taskRepository;
    private final MeetingRepository meetingRepository;
    private final ExtractionJobRepository extractionJobRepository;
    private final OllamaService ollamaService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ExtractionJob startLatestExtraction(Long userId) {
        return startExtraction(userId, 1);
    }

    public ExtractionJob startExtraction(Long userId, int limit) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        ExtractionJob job = new ExtractionJob();
        job.setUser(user);
        job.setStatus("PENDING");
        job = extractionJobRepository.save(job);

        int safeLimit = limit <= 0 ? 10 : Math.min(limit, 50);
        extractAsync(job.getId(), userId, safeLimit);
        return job;
    }

    public ExtractionJob getJob(Long jobId) {
        return extractionJobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    public List<TaskEntity> getTasks(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return taskRepository.findByUserOrderByExtractedAtDesc(user);
    }

    public List<MeetingEntity> getMeetings(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return meetingRepository.findByUserOrderByExtractedAtDesc(user);
    }

    @Async
    public void extractAsync(Long jobId, Long userId, int limit) {
        ExtractionJob job = extractionJobRepository.findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found"));

        try {
            job.setStatus("RUNNING");
            extractionJobRepository.save(job);

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

            List<EmailEntity> emails = emailRepository.findByUserOrderByReceivedAtDesc(user);
            if (emails == null || emails.isEmpty()) {
                job.setStatus("COMPLETED");
                job.setSourceEmailCount(0);
                job.setTaskCount(0);
                job.setMeetingCount(0);
                job.setCompletedAt(LocalDateTime.now());
                extractionJobRepository.save(job);
                return;
            }

            List<EmailEntity> limited = emails.subList(0, Math.min(limit, emails.size()));
            job.setSourceEmailCount(limited.size());
            extractionJobRepository.save(job);

            String prompt = buildPrompt(limited);
            String response = ollamaService.generateResponse(prompt, "", 1200, false);
            ExtractionResult result = parseResponse(response);

            int taskCount = persistTasks(user, result.tasks);
            int meetingCount = persistMeetings(user, result.meetings);

            job.setStatus("COMPLETED");
            job.setTaskCount(taskCount);
            job.setMeetingCount(meetingCount);
            job.setCompletedAt(LocalDateTime.now());
            extractionJobRepository.save(job);
        } catch (Exception e) {
            log.error("Extraction failed", e);
            job.setStatus("FAILED");
            job.setErrorMessage(e.getMessage());
            job.setCompletedAt(LocalDateTime.now());
            extractionJobRepository.save(job);
        }
    }

    private int persistTasks(User user, List<TaskData> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (TaskData data : tasks) {
            if (data == null || data.title == null || data.title.isBlank()) {
                continue;
            }
            String sourceEmailId = trimToNull(data.sourceEmailId);
            String title = data.title.trim();
            LocalDateTime dueAt = parseDateTime(data.dueAt);
            boolean exists = sourceEmailId != null
                ? taskRepository.existsByUserAndTitleAndDueAtAndSourceEmailId(user, title, dueAt, sourceEmailId)
                : taskRepository.existsByUserAndTitleAndSourceEmailId(user, title, null);
            if (exists) {
                continue;
            }
            TaskEntity task = new TaskEntity();
            task.setUser(user);
            task.setTitle(title);
            task.setDescription(trimToNull(data.description));
            task.setDueAt(dueAt);
            task.setStatus(trimToNull(data.status) != null ? data.status.trim() : "OPEN");
            task.setSourceEmailId(sourceEmailId);
            task.setExtractedAt(LocalDateTime.now());
            taskRepository.save(task);
            count++;
        }
        return count;
    }

    private int persistMeetings(User user, List<MeetingData> meetings) {
        if (meetings == null || meetings.isEmpty()) {
            return 0;
        }
        int count = 0;
        for (MeetingData data : meetings) {
            if (data == null || data.title == null || data.title.isBlank()) {
                continue;
            }
            String sourceEmailId = trimToNull(data.sourceEmailId);
            String title = data.title.trim();
            LocalDateTime startAt = parseDateTime(data.startAt);
            boolean exists = sourceEmailId != null
                ? meetingRepository.existsByUserAndTitleAndStartAtAndSourceEmailId(user, title, startAt, sourceEmailId)
                : meetingRepository.existsByUserAndTitleAndSourceEmailId(user, title, null);
            if (exists) {
                continue;
            }
            MeetingEntity meeting = new MeetingEntity();
            meeting.setUser(user);
            meeting.setTitle(title);
            meeting.setStartAt(startAt);
            meeting.setEndAt(parseDateTime(data.endAt));
            meeting.setLocation(trimToNull(data.location));
            meeting.setAttendees(trimToNull(data.attendees));
            meeting.setSourceEmailId(sourceEmailId);
            meeting.setExtractedAt(LocalDateTime.now());
            meetingRepository.save(meeting);
            count++;
        }
        return count;
    }

    private String buildPrompt(List<EmailEntity> emails) {
        StringBuilder builder = new StringBuilder();
        builder.append("Extract tasks and meetings from the emails below. ");
        builder.append("Only include items explicitly stated in the email text. ");
        builder.append("If unsure, omit the item.\n\n");
        builder.append("Output JSON only, no extra text. Format:\n");
        builder.append("{\n");
        builder.append("  \"tasks\": [\n");
        builder.append("    {\"title\":\"\",\"description\":\"\",\"dueAt\":\"YYYY-MM-DDTHH:MM\",\"status\":\"OPEN\",\"sourceEmailId\":\"\"}\n");
        builder.append("  ],\n");
        builder.append("  \"meetings\": [\n");
        builder.append("    {\"title\":\"\",\"startAt\":\"YYYY-MM-DDTHH:MM\",\"endAt\":\"YYYY-MM-DDTHH:MM\",\"location\":\"\",\"attendees\":\"\",\"sourceEmailId\":\"\"}\n");
        builder.append("  ]\n");
        builder.append("}\n\n");
        builder.append("Rules:\n");
        builder.append("- Use null or empty string if a field is not present.\n");
        builder.append("- Keep dueAt/startAt/endAt in ISO-8601 if present.\n");
        builder.append("- Use sourceEmailId from the email id field below.\n\n");

        int index = 1;
        for (EmailEntity email : emails) {
            builder.append("Email ").append(index++).append(":\n");
            builder.append("id: ").append(email.getGmailMessageId()).append("\n");
            builder.append("subject: ").append(safe(email.getSubject())).append("\n");
            builder.append("from: ").append(safe(email.getSender())).append("\n");
            builder.append("receivedAt: ").append(email.getReceivedAt()).append("\n");
            builder.append("body: ").append(trimBody(email.getBody())).append("\n\n");
        }

        return builder.toString();
    }

    private ExtractionResult parseResponse(String response) throws Exception {
        String cleaned = cleanupJsonResponse(response);
        return objectMapper.readValue(cleaned, ExtractionResult.class);
    }

    private String cleanupJsonResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "{\"tasks\":[],\"meetings\":[]}";
        }
        String cleaned = response.trim();
        cleaned = cleaned.replaceAll("^```json\\s*", "");
        cleaned = cleaned.replaceAll("^```\\s*", "");
        cleaned = cleaned.replaceAll("\\s*```$", "");
        cleaned = cleaned.trim();

        int startIndex = cleaned.indexOf('{');
        int endIndex = cleaned.lastIndexOf('}');
        if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
            return "{\"tasks\":[],\"meetings\":[]}";
        }
        cleaned = cleaned.substring(startIndex, endIndex + 1);
        return cleaned.trim();
    }

    private String trimBody(String body) {
        if (body == null) {
            return "";
        }
        String trimmed = body.replaceAll("\\s+", " ").trim();
        if (trimmed.length() > 1200) {
            return trimmed.substring(0, 1200) + "...";
        }
        return trimmed;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String cleaned = value.trim();
        try {
            return LocalDateTime.parse(cleaned, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception ignored) {
        }
        try {
            return LocalDate.parse(cleaned, DateTimeFormatter.ISO_DATE).atStartOfDay();
        } catch (Exception ignored) {
        }
        return null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ExtractionResult {
        private List<TaskData> tasks = new ArrayList<>();
        private List<MeetingData> meetings = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class TaskData {
        private String title;
        private String description;
        private String dueAt;
        private String status;
        private String sourceEmailId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MeetingData {
        private String title;
        private String startAt;
        private String endAt;
        private String location;
        private String attendees;
        private String sourceEmailId;
    }
}
