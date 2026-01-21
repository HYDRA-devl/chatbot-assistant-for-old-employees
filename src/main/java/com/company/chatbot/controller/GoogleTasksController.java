package com.company.chatbot.controller;

import com.company.chatbot.service.GoogleTasksService;
import com.google.api.services.tasks.model.Task;
import com.google.api.services.tasks.model.TaskList;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/google-tasks")
@RequiredArgsConstructor
public class GoogleTasksController {

    private final GoogleTasksService googleTasksService;

    @GetMapping("/lists")
    public ResponseEntity<List<TaskList>> getTaskLists() {
        return ResponseEntity.ok(googleTasksService.listTaskLists());
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> getTasks(
        @RequestParam(required = false) String taskListId,
        @RequestParam(required = false) Integer limit
    ) {
        return ResponseEntity.ok(googleTasksService.listTasks(taskListId, limit));
    }
}
