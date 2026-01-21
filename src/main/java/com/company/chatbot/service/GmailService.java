package com.company.chatbot.service;

import com.company.chatbot.entity.EmailEntity;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.EmailRepository;
import com.company.chatbot.repository.UserRepository;
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
                    if (emailRepository.findByUserAndGmailMessageId(user, messageRef.getId()).isPresent()) {
                        continue;
                    }

                    Message message = gmail.users().messages()
                        .get("me", messageRef.getId())
                        .setFormat("full")
                        .execute();

                    EmailEntity email = new EmailEntity();
                    email.setUser(user);
                    email.setGmailMessageId(message.getId());
                    email.setSubject(getHeaderValue(message, "Subject"));
                    email.setSender(getHeaderValue(message, "From"));
                    email.setBody(extractPlainText(message.getPayload()));
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
        if ("text/plain".equalsIgnoreCase(part.getMimeType())
            && part.getBody() != null
            && part.getBody().getData() != null) {
            return decodeBody(part.getBody().getData());
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

    private String decodeBody(String data) {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(data);
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
