package com.company.chatbot.service;

import com.company.chatbot.dto.ChatRequest;
import com.company.chatbot.dto.ChatResponse;
import com.company.chatbot.entity.ChatMessage;
import com.company.chatbot.entity.Conversation;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.ChatMessageRepository;
import com.company.chatbot.repository.ConversationRepository;
import com.company.chatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final OllamaService ollamaService;
    private final ChatMessageRepository chatMessageRepository;
    private final ConversationRepository conversationRepository;
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

    /**
     * Get messages for a specific conversation
     */
    public List<ChatMessage> getConversationMessages(Long conversationId) {
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    /**
     * Process streaming message with token-by-token response
     */
    public void processStreamingMessage(Long userId, String message, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                StringBuilder fullResponse = new StringBuilder();
                
                // Find user
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // Get context
                String context = getContextForQuery(message);

                // Stream response from Ollama
                ollamaService.generateStreamingResponse(
                    message,
                    context,
                    // On each token received
                    token -> {
                        try {
                            fullResponse.append(token);
                            // JSON-encode token to preserve spaces and special chars in SSE
                            String escapedToken = token
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r")
                                .replace("\t", "\\t");
                            String jsonToken = String.format("{\"token\":\"%s\"}", escapedToken);
                            log.debug("Sending token: '{}'", token);
                            emitter.send(SseEmitter.event()
                                .name("token")
                                .data(jsonToken));
                        } catch (IOException e) {
                            log.error("Error sending token: ", e);
                            emitter.completeWithError(e);
                        }
                    },
                    // On completion
                    () -> {
                        try {
                            long responseTime = System.currentTimeMillis() - startTime;
                            
                            // Award points
                            int pointsEarned = gamificationService.awardPointsForMessage(user);

                            // Save chat message
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setUser(user);
                            chatMessage.setUserMessage(message);
                            chatMessage.setBotResponse(fullResponse.toString());
                            chatMessage.setResponseTimeMs(responseTime);
                            chatMessage.setPointsEarned(pointsEarned);
                            chatMessageRepository.save(chatMessage);

                            // Update user stats
                            user.setMessagesSent(user.getMessagesSent() + 1);
                            userRepository.save(user);

                            // Check achievements
                            gamificationService.checkAndAwardAchievements(user);

                            // Send completion event with points
                            emitter.send(SseEmitter.event()
                                .name("complete")
                                .data(String.format("{\"pointsEarned\":%d,\"messageId\":%d}", 
                                    pointsEarned, chatMessage.getId())));
                            
                            emitter.complete();
                            
                            log.info("Streaming completed for user {} in {}ms", user.getUsername(), responseTime);
                        } catch (Exception e) {
                            log.error("Error completing stream: ", e);
                            emitter.completeWithError(e);
                        }
                    }
                );
                
            } catch (Exception e) {
                log.error("Error in streaming message: ", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("Error: " + e.getMessage()));
                } catch (IOException ex) {
                    log.error("Error sending error event: ", ex);
                }
                emitter.completeWithError(e);
            }
        });
    }

    /**
     * Process streaming message with conversation context
     */
    public void processStreamingMessageWithConversation(Long userId, Long conversationId, String message, SseEmitter emitter) {
        CompletableFuture.runAsync(() -> {
            try {
                long startTime = System.currentTimeMillis();
                StringBuilder fullResponse = new StringBuilder();
                
                // Find user and conversation
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found"));
                
                Conversation conversation = conversationRepository.findById(conversationId)
                        .orElseThrow(() -> new RuntimeException("Conversation not found"));

                // Get context from conversation history
                String context = getConversationContext(conversation);

                // Stream response from Ollama
                ollamaService.generateStreamingResponse(
                    message,
                    context,
                    // On each token received
                    token -> {
                        try {
                            fullResponse.append(token);
                            // JSON-encode token to preserve spaces and special chars in SSE
                            String escapedToken = token
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\n", "\\n")
                                .replace("\r", "\\r")
                                .replace("\t", "\\t");
                            String jsonToken = String.format("{\"token\":\"%s\"}", escapedToken);
                            log.debug("Sending token: '{}'", token);
                            emitter.send(SseEmitter.event()
                                .name("token")
                                .data(jsonToken));
                        } catch (IOException e) {
                            log.error("Error sending token: ", e);
                            emitter.completeWithError(e);
                        }
                    },
                    // On completion
                    () -> {
                        try {
                            long responseTime = System.currentTimeMillis() - startTime;
                            
                            // Award points
                            int pointsEarned = gamificationService.awardPointsForMessage(user);

                            // Save chat message with conversation link
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setUser(user);
                            chatMessage.setConversation(conversation);
                            chatMessage.setUserMessage(message);
                            chatMessage.setBotResponse(fullResponse.toString());
                            chatMessage.setResponseTimeMs(responseTime);
                            chatMessage.setPointsEarned(pointsEarned);
                            chatMessageRepository.save(chatMessage);

                            // Update conversation message count
                            conversation.incrementMessageCount();
                            conversationRepository.save(conversation);

                            // Update user stats
                            user.setMessagesSent(user.getMessagesSent() + 1);
                            userRepository.save(user);

                            // Check achievements
                            gamificationService.checkAndAwardAchievements(user);

                            // Send completion event with points
                            emitter.send(SseEmitter.event()
                                .name("complete")
                                .data(String.format("{\"pointsEarned\":%d,\"messageId\":%d}", 
                                    pointsEarned, chatMessage.getId())));
                            
                            emitter.complete();
                            
                            log.info("Streaming completed for user {} in conversation {} ({}ms)", 
                                user.getUsername(), conversationId, responseTime);
                        } catch (Exception e) {
                            log.error("Error completing stream: ", e);
                            emitter.completeWithError(e);
                        }
                    }
                );
                
            } catch (Exception e) {
                log.error("Error in streaming message: ", e);
                try {
                    emitter.send(SseEmitter.event()
                        .name("error")
                        .data("Error: " + e.getMessage()));
                } catch (IOException ex) {
                    log.error("Error sending error event: ", ex);
                }
                emitter.completeWithError(e);
            }
        });
    }

    /**
     * Get conversation context from previous messages
     */
    private String getConversationContext(Conversation conversation) {
        List<ChatMessage> previousMessages = chatMessageRepository
            .findByConversationOrderByCreatedAtAsc(conversation);
        
        if (previousMessages.isEmpty()) {
            return "";
        }
        
        // Build context from last 3-5 messages to avoid token limits
        int startIndex = Math.max(0, previousMessages.size() - 5);
        return previousMessages.subList(startIndex, previousMessages.size()).stream()
            .map(m -> "User: " + m.getUserMessage() + "\nAI: " + m.getBotResponse())
            .collect(Collectors.joining("\n\n"));
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
