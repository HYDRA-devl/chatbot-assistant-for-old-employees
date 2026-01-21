package com.company.chatbot.controller;

import com.company.chatbot.dto.ChatRequest;
import com.company.chatbot.dto.ChatResponse;
import com.company.chatbot.entity.ChatMessage;
import com.company.chatbot.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @GetMapping("/conversation/{conversationId}/messages")
    public ResponseEntity<List<ChatMessage>> getConversationMessages(
            @PathVariable Long conversationId) {
        log.info("Getting messages for conversation: {}", conversationId);
        List<ChatMessage> messages = chatService.getConversationMessages(conversationId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
            @RequestParam Long userId,
            @RequestParam String message) {
        
        log.info("Streaming chat message from user: {}", userId);
        
        SseEmitter emitter = new SseEmitter(180000L); // 3 minute timeout
        
        // Process streaming in background
        chatService.processStreamingMessage(userId, message, emitter);
        
        return emitter;
    }

    @GetMapping(value = "/stream/conversation", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessageWithConversation(
            @RequestParam Long userId,
            @RequestParam Long conversationId,
            @RequestParam String message) {
        
        log.info("Streaming chat message from user: {} in conversation: {}", userId, conversationId);
        
        SseEmitter emitter = new SseEmitter(180000L); // 3 minute timeout
        
        // Process streaming with conversation context
        chatService.processStreamingMessageWithConversation(userId, conversationId, message, emitter);
        
        return emitter;
    }
}
