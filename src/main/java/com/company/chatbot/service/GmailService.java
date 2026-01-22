package com.company.chatbot.service;

import com.company.chatbot.entity.EmailEntity;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.EmailRepository;
import com.company.chatbot.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.gmail.Gmail;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GmailService {

    private final UserRepository userRepository;
    private final EmailRepository emailRepository;
    private final GoogleAuthService googleAuthService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    private volatile Gmail gmailClient;
    private final Object gmailClientLock = new Object();

    private static final String APPLICATION_NAME = "Employee Chatbot";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    public void connect() {
        getOrInitGmailClient();
    }

    public List<EmailEntity> fetchRecentEmails(Long userId, int maxResults) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            Gmail gmail = getOrInitGmailClient();

            ListMessagesResponse response = gmail.users().messages()
                .list("me")
                .setMaxResults((long) maxResults)
                .execute();

            List<Message> messages = response.getMessages();
            if (messages != null) {
                for (Message messageRef : messages) {
                    EmailEntity email = emailRepository.findByUserAndGmailMessageId(user, messageRef.getId())
                        .orElse(null);

                    if (email != null && email.getBody() != null && !email.getBody().isBlank()
                        && !isLikelyHtml(email.getBody())) {
                        continue;
                    }
                    if (email != null && email.getBody() != null && isLikelyHtml(email.getBody())) {
                        log.debug("Refreshing email {} due to HTML/CSS content", messageRef.getId());
                    }

                    Message message = gmail.users().messages()
                        .get("me", messageRef.getId())
                        .setFormat("full")
                        .execute();

                    if (email == null) {
                        email = new EmailEntity();
                        email.setUser(user);
                        email.setGmailMessageId(message.getId());
                    }

                    email.setSubject(getHeaderValue(message, "Subject"));
                    email.setSender(getHeaderValue(message, "From"));
                    String extracted = extractPlainText(message.getPayload());
                    email.setBody(cleanEmailBody(extracted));
                    log.debug("Email {} body length after clean: {}", message.getId(), email.getBody() == null ? 0 : email.getBody().length());
                    email.setReceivedAt(parseReceivedAt(message));

                    emailRepository.save(email);
                }
            }

            return emailRepository.findByUserOrderByReceivedAtDesc(user);
        } catch (Exception e) {
            log.error("Failed to fetch Gmail emails", e);
            throw new RuntimeException("Failed to fetch Gmail emails");
        }
    }

    public EmailSummary summarizeEmail(Long userId, Long emailId) {
        EmailEntity email = emailRepository.findByIdAndUserId(emailId, userId)
            .orElseThrow(() -> new RuntimeException("Email not found"));

        String subject = email.getSubject() == null ? "" : email.getSubject();
        String sender = email.getSender() == null ? "" : email.getSender();
        String body = email.getBody() == null ? "" : email.getBody();
        body = cleanEmailBody(body);
        body = normalizeEmailText(body);
        body = trimForGemini(body, 2000);

        log.info("Summarizing email {} for user {}. Subject length: {}, Body length: {}", emailId, userId, subject.length(), body.length());
        log.debug("Body length after trim for Gemini: {}", body.length());
        log.debug("Email body preview (email {}): {}", emailId, body.length() > 240 ? body.substring(0, 240) + "..." : body);
        String prompt = "Summarize the email and extract useful information. "
            + "Do not copy the email verbatim; rephrase concisely. "
            + "Return JSON only in this exact format: {\"summary\":\"...\",\"usefulInfo\":[\"...\"]}. "
            + "Summary should be 2-4 sentences. UsefulInfo should be 2-5 bullet-like items. "
            + "Include key dates, actions, links, and decisions if present. "
            + "Subject: " + subject + ". Sender: " + sender + ".";

        String response = geminiService.generateResponse(prompt, body, 6000);
        log.info("Gemini summary raw response (email {}): {}", emailId, response);
        return parseSummaryResponse(response, body);
    }

    private EmailSummary parseSummaryResponse(String response, String body) {
        log.debug("Parsing Gemini summary. Raw length: {}", response == null ? 0 : response.length());
        String json = extractJson(response);
        log.debug("Extracted JSON for summary: {}", json);
        if (json == null || json.isBlank()) {
            log.warn("Gemini summary JSON missing; using fallback summary");
            return fallbackSummary(body);
        }

        try {
            JsonNode root = objectMapper.readTree(json);
            String summary = root.path("summary").asText("");
            List<String> usefulInfo = new java.util.ArrayList<>();
            JsonNode usefulNode = root.path("usefulInfo");
            if (usefulNode.isArray()) {
                for (JsonNode item : usefulNode) {
                    String value = item.asText("");
                    if (!value.isBlank()) {
                        usefulInfo.add(value);
                    }
                }
            } else if (usefulNode.isTextual()) {
                String value = usefulNode.asText("");
                if (!value.isBlank()) {
                    usefulInfo.add(value);
                }
            }

            if (summary.isBlank()) {
                log.warn("Gemini summary empty; using fallback summary");
                return fallbackSummary(body);
            }

            return new EmailSummary(summary, usefulInfo);
        } catch (Exception e) {
            log.warn("Failed to parse Gemini email summary", e);
            return fallbackSummary(body);
        }
    }

    private EmailSummary fallbackSummary(String body) {
        String trimmed = body == null ? "" : body.trim();
        String summary = trimmed.isBlank()
            ? "No email content available."
            : (trimmed.length() > 240 ? trimmed.substring(0, 240) + "..." : trimmed);
        return new EmailSummary(summary, List.of());
    }

    private String extractJson(String response) {
        if (response == null) {
            return null;
        }
        String cleaned = response;
        if (cleaned.contains("```")) {
            cleaned = cleaned.replace("```json", "");
            cleaned = cleaned.replace("```", "");
        }
        int start = cleaned.indexOf('{');
        if (start < 0) {
            return null;
        }
        int depth = 0;
        boolean inString = false;
        for (int i = start; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (c == '"' && (i == 0 || cleaned.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (inString) {
                continue;
            }
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return cleaned.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    public static class EmailSummary {
        private String summary;
        private List<String> usefulInfo;

        public EmailSummary() {
        }

        public EmailSummary(String summary, List<String> usefulInfo) {
            this.summary = summary;
            this.usefulInfo = usefulInfo;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public List<String> getUsefulInfo() {
            return usefulInfo;
        }

        public void setUsefulInfo(List<String> usefulInfo) {
            this.usefulInfo = usefulInfo;
        }
    }

    private Gmail getOrInitGmailClient() {
        if (gmailClient != null) {
            return gmailClient;
        }
        synchronized (gmailClientLock) {
            if (gmailClient != null) {
                return gmailClient;
            }
            try {
                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                gmailClient = new Gmail.Builder(httpTransport, JSON_FACTORY, googleAuthService.getCredential())
                    .setApplicationName(APPLICATION_NAME)
                    .build();
                return gmailClient;
            } catch (Exception e) {
                log.error("Failed to initialize Gmail client", e);
                throw new RuntimeException("Failed to initialize Gmail client");
            }
        }
    }

    private String getHeaderValue(Message message, String name) {
        if (message.getPayload() == null || message.getPayload().getHeaders() == null) {
            return "";
        }
        for (MessagePartHeader header : message.getPayload().getHeaders()) {
            if (name.equalsIgnoreCase(header.getName())) {
                return header.getValue();
            }
        }
        return "";
    }

    private LocalDateTime parseReceivedAt(Message message) {
        Long internalDate = message.getInternalDate();
        if (internalDate == null) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(internalDate), ZoneId.systemDefault());
    }

    private String extractPlainText(MessagePart part) {
        if (part == null) {
            return "";
        }
        if (part.getBody() != null && part.getBody().getData() != null) {
            String mimeType = part.getMimeType();
            log.debug("Extracting Gmail part with mimeType: {}", mimeType);
            if ("text/plain".equalsIgnoreCase(mimeType)) {
                String text = decodeBody(part.getBody().getData());
                return cleanEmailBody(text);
            }
            if ("text/html".equalsIgnoreCase(mimeType)) {
                return cleanEmailBody(decodeBody(part.getBody().getData()));
            }
        }
        if (part.getParts() != null) {
            for (MessagePart child : part.getParts()) {
                String text = extractPlainText(child);
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return "";
    }

    private boolean isLikelyHtml(String text) {
        if (text == null) {
            return false;
        }
        String lower = text.toLowerCase();
        if (lower.contains("<html")
            || lower.contains("<body")
            || lower.contains("<div")
            || lower.contains("<table")
            || lower.contains("<style")
            || lower.contains("</")) {
            return true;
        }
        if (lower.contains("@font-face")
            || lower.contains("font-family")
            || lower.contains("background:")
            || lower.contains("color:")) {
            return true;
        }
        return lower.contains("{") && lower.contains("}") && lower.contains(":");
    }

    private String normalizeEmailText(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text;
        cleaned = cleaned.replace("&nbsp;", " ");
        cleaned = cleaned.replace("&amp;", "&");
        cleaned = cleaned.replace("&lt;", "<");
        cleaned = cleaned.replace("&gt;", ">");
        cleaned = cleaned.replace("&quot;", "\"");
        cleaned = cleaned.replace("&#39;", "'");
        cleaned = cleaned.replace("’", "'");
        cleaned = cleaned.replace("‘", "'");
        cleaned = cleaned.replace("“", "\"");
        cleaned = cleaned.replace("”", "\"");
        cleaned = cleaned.replace(" ", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private String trimForGemini(String text, int maxChars) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxChars) {
            return text;
        }
        return text.substring(0, maxChars) + "...";
    }

    private String cleanEmailBody(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String cleaned = text;
        if (isLikelyHtml(cleaned)) {
            cleaned = stripHtml(cleaned);
        }
        cleaned = removeCssArtifacts(cleaned);
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private String removeCssArtifacts(String text) {
        String cleaned = text;
        cleaned = cleaned.replaceAll("(?is)<style.*?>.*?</style>", " ");
        cleaned = cleaned.replaceAll("(?is)@font-face\\s*\\{.*?\\}", " ");
        cleaned = cleaned.replaceAll("(?is)[.#][^\\n{]{1,120}\\{.*?\\}", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s*[.#@][^\\n]*;\\s*$", " ");
        cleaned = cleaned.replaceAll("(?m)^\\s*[a-zA-Z-]+\\s*:\\s*[^\\n;]+;\\s*$", " ");
        return cleaned;
    }

    private String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String cleaned = html.replaceAll("(?is)<(script|style).*?>.*?</\\1>", " ");
        cleaned = cleaned.replaceAll("<[^>]+>", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned;
    }

    private String decodeBody(String data) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(data);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
