package com.company.chatbot.controller;

import com.company.chatbot.dto.ChatRequest;
import com.company.chatbot.dto.ChatResponse;
import com.company.chatbot.entity.ChatMessage;
import com.company.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(@RequestBody ChatRequest request) {
        log.info("Received chat message from user: {}", request.getUserId());
        ChatResponse response = chatService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable Long userId) {
        List<ChatMessage> history = chatService.getChatHistory(userId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/{userId}/recent")
    public ResponseEntity<List<ChatMessage>> getRecentChatHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<ChatMessage> history = chatService.getRecentChatHistory(userId, limit);
        return ResponseEntity.ok(history);
    }
}
