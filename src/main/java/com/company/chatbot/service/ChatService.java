package com.company.chatbot.service;

import com.company.chatbot.dto.ChatRequest;
import com.company.chatbot.dto.ChatResponse;
import com.company.chatbot.entity.ChatMessage;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.ChatMessageRepository;
import com.company.chatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final OllamaService ollamaService;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final GamificationService gamificationService;

    @Transactional
    public ChatResponse processMessage(ChatRequest request) {
        long startTime = System.currentTimeMillis();
        
        // Find user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get context from RAG (placeholder for now)
        String context = getContextForQuery(request.getMessage());

        // Generate response from Ollama
        String botResponse = ollamaService.generateResponse(request.getMessage(), context);

        // Calculate response time
        long responseTime = System.currentTimeMillis() - startTime;

        // Award points based on message interaction
        int pointsEarned = gamificationService.awardPointsForMessage(user);

        // Save chat message
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setUser(user);
        chatMessage.setUserMessage(request.getMessage());
        chatMessage.setBotResponse(botResponse);
        chatMessage.setResponseTimeMs(responseTime);
        chatMessage.setPointsEarned(pointsEarned);
        
        chatMessage = chatMessageRepository.save(chatMessage);

        // Update user statistics
        user.setMessagesSent(user.getMessagesSent() + 1);
        userRepository.save(user);

        // Check for achievement progress
        gamificationService.checkAndAwardAchievements(user);

        log.info("Message processed for user {} in {}ms", user.getUsername(), responseTime);

        return new ChatResponse(botResponse, chatMessage.getId(), pointsEarned, responseTime);
    }

    public List<ChatMessage> getChatHistory(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return chatMessageRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public List<ChatMessage> getRecentChatHistory(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return chatMessageRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    // Placeholder for RAG implementation
    private String getContextForQuery(String query) {
        // TODO: Implement RAG logic here
        // This will involve:
        // 1. Embedding the query
        // 2. Searching vector database for relevant documents
        // 3. Returning top-k relevant context chunks
        return "";
    }
}
