package com.company.chatbot.controller;

import com.company.chatbot.entity.ExtractionJob;
import com.company.chatbot.service.EmailExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/extractions")
@RequiredArgsConstructor
public class ExtractionController {

    private final EmailExtractionService emailExtractionService;

        @PostMapping("/tasks-meetings/latest")
    public ResponseEntity<ExtractionJob> startLatestExtraction(
        @RequestParam Long userId
    ) {
        return ResponseEntity.ok(emailExtractionService.startLatestExtraction(userId));
    }

@PostMapping("/tasks-meetings")
    public ResponseEntity<ExtractionJob> startExtraction(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(emailExtractionService.startExtraction(userId, limit));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<ExtractionJob> getJob(@PathVariable Long jobId) {
        return ResponseEntity.ok(emailExtractionService.getJob(jobId));
    }
}
