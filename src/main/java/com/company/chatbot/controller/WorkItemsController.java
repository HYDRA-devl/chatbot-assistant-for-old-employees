package com.company.chatbot.controller;

import com.company.chatbot.entity.MeetingEntity;
import com.company.chatbot.entity.TaskEntity;
import com.company.chatbot.service.EmailExtractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkItemsController {

    private final EmailExtractionService emailExtractionService;

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskEntity>> getTasks(@RequestParam Long userId) {
        return ResponseEntity.ok(emailExtractionService.getTasks(userId));
    }

    @GetMapping("/meetings")
    public ResponseEntity<List<MeetingEntity>> getMeetings(@RequestParam Long userId) {
        return ResponseEntity.ok(emailExtractionService.getMeetings(userId));
    }
}
