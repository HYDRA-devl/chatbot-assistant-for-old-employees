package com.company.chatbot.controller;

import com.company.chatbot.dto.ActivitySyncResult;
import com.company.chatbot.dto.CompleteMeetingRequest;
import com.company.chatbot.dto.CompleteTaskRequest;
import com.company.chatbot.dto.CompletedActivityResponse;
import com.company.chatbot.service.ActivityCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityCompletionController {

    private final ActivityCompletionService activityCompletionService;

    @PostMapping("/tasks/complete")
    public ResponseEntity<ActivitySyncResult> completeTask(@RequestBody CompleteTaskRequest request) {
        return ResponseEntity.ok(activityCompletionService.completeTask(request));
    }

    @PostMapping("/meetings/complete")
    public ResponseEntity<ActivitySyncResult> completeMeeting(@RequestBody CompleteMeetingRequest request) {
        return ResponseEntity.ok(activityCompletionService.completeMeeting(request));
    }

    @GetMapping("/completed")
    public ResponseEntity<CompletedActivityResponse> getCompleted(@RequestParam Long userId) {
        return ResponseEntity.ok(activityCompletionService.getCompletedActivities(userId));
    }
}
