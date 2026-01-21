package com.company.chatbot.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.tasks.TasksScopes;
import com.google.api.services.calendar.CalendarScopes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class GoogleAuthService {

    private static final String TOKENS_DIRECTORY = "tokens";
    private static final String SCOPES_FILE = "scopes.txt";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(
        GmailScopes.GMAIL_READONLY,
        TasksScopes.TASKS,
        CalendarScopes.CALENDAR_READONLY
    );

    @Value("${gmail.oauth.receiver-port:8888}")
    private int oauthReceiverPort;

    private volatile Credential credential;
    private final Object credentialLock = new Object();

    public Credential getCredential() {
        if (credential != null) {
            return credential;
        }
        synchronized (credentialLock) {
            if (credential != null) {
                return credential;
            }
            try {
                ensureScopesMatch();

                final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
                GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                    JSON_FACTORY,
                    new InputStreamReader(new ClassPathResource("credentials.json").getInputStream())
                );

                GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                    .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY)))
                    .setAccessType("offline")
                    .build();

                LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                    .setPort(oauthReceiverPort)
                    .build();

                credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
                writeScopesMarker();
                return credential;
            } catch (Exception e) {
                log.error("Failed to initialize Google OAuth credential", e);
                throw new RuntimeException("Failed to initialize Google OAuth credential");
            }
        }
    }

    public List<String> getScopes() {
        return SCOPES;
    }

    private void ensureScopesMatch() throws IOException {
        Path tokensDir = Paths.get(TOKENS_DIRECTORY);
        Path scopesPath = tokensDir.resolve(SCOPES_FILE);
        String desired = String.join(" ", SCOPES);

        if (Files.exists(scopesPath)) {
            String current = Files.readString(scopesPath, StandardCharsets.UTF_8).trim();
            if (!current.equals(desired)) {
                deleteTokensDirectory(tokensDir);
                Files.createDirectories(tokensDir);
            }
        } else if (Files.exists(tokensDir)) {
            // No scopes marker means old tokens: reset to force re-consent.
            deleteTokensDirectory(tokensDir);
            Files.createDirectories(tokensDir);
        } else {
            Files.createDirectories(tokensDir);
        }
    }

    private void writeScopesMarker() {
        try {
            Path tokensDir = Paths.get(TOKENS_DIRECTORY);
            Files.createDirectories(tokensDir);
            Path scopesPath = tokensDir.resolve(SCOPES_FILE);
            Files.writeString(scopesPath, String.join(" ", SCOPES), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Failed to write scopes marker", e);
        }
    }

    private void deleteTokensDirectory(Path tokensDir) throws IOException {
        if (!Files.exists(tokensDir)) {
            return;
        }
        Files.walk(tokensDir)
            .sorted(Comparator.reverseOrder())
            .forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }
}
