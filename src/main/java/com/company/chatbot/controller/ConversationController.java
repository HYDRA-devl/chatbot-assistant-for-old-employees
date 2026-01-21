package com.company.chatbot.controller;

import com.company.chatbot.entity.Conversation;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.UserRepository;
import com.company.chatbot.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    public ResponseEntity<Conversation> createConversation(@RequestParam Long userId) {
        log.info("Creating new conversation for user: {}", userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Conversation conversation = conversationService.createConversation(user);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/active")
    public ResponseEntity<Conversation> getActiveConversation(@RequestParam Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Conversation conversation = conversationService.getOrCreateActiveConversation(user);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Conversation>> getUserConversations(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Conversation> conversations = conversationService.getUserConversations(user);
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<Conversation> getConversation(@PathVariable Long conversationId) {
        Conversation conversation = conversationService.getConversation(conversationId);
        return ResponseEntity.ok(conversation);
    }

    @PostMapping("/{conversationId}/end")
    public ResponseEntity<Conversation> endConversation(@PathVariable Long conversationId) {
        log.info("Ending conversation: {}", conversationId);
        Conversation conversation = conversationService.endConversation(conversationId);
        return ResponseEntity.ok(conversation);
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long conversationId) {
        log.info("Deleting conversation: {}", conversationId);
        conversationService.deleteConversation(conversationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{conversationId}/archive")
    public ResponseEntity<Void> archiveConversation(@PathVariable Long conversationId) {
        log.info("Archiving conversation: {}", conversationId);
        conversationService.archiveConversation(conversationId);
        return ResponseEntity.ok().build();
    }
}
