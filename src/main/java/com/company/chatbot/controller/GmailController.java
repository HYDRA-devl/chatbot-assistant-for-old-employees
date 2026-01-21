package com.company.chatbot.controller;

import com.company.chatbot.entity.EmailEntity;
import com.company.chatbot.service.GmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/gmail")
@RequiredArgsConstructor
public class GmailController {

    private final GmailService gmailService;

    @GetMapping("/connect")
    public ResponseEntity<String> connect() {
        gmailService.connect();
        return ResponseEntity.ok("Gmail connected. Complete OAuth in the opened browser if prompted.");
    }

    @GetMapping("/emails")
    public ResponseEntity<List<EmailEntity>> getEmails(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(gmailService.fetchRecentEmails(userId, limit));
    }
}
