package com.company.chatbot.service;

import com.company.chatbot.entity.Conversation;
import com.company.chatbot.entity.ConversationStatus;
import com.company.chatbot.entity.ChatMessage;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.ConversationRepository;
import com.company.chatbot.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final OllamaService ollamaService;

    /**
     * Create a new conversation for a user
     */
    @Transactional
    public Conversation createConversation(User user) {
        Conversation conversation = new Conversation();
        conversation.setUser(user);
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation.setStartedAt(LocalDateTime.now());
        conversation.setMessageCount(0);
        
        conversation = conversationRepository.save(conversation);
        log.info("Created new conversation {} for user {}", conversation.getId(), user.getUsername());
        
        return conversation;
    }

    /**
     * Get or create active conversation for user
     * If user has an active conversation, return it
     * Otherwise create a new one
     */
    @Transactional
    public Conversation getOrCreateActiveConversation(User user) {
        Optional<Conversation> activeConversation = conversationRepository
            .findByUserAndStatus(user, ConversationStatus.ACTIVE);
        
        if (activeConversation.isPresent()) {
            return activeConversation.get();
        }
        
        return createConversation(user);
    }

    /**
     * End a conversation and extract topic
     */
    @Transactional
    public Conversation endConversation(Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        conversation.setStatus(ConversationStatus.COMPLETED);
        conversation.setEndedAt(LocalDateTime.now());
        
        // Extract topic from conversation messages
        String topic = extractTopicFromConversation(conversation);
        conversation.setTopic(topic);
        
        // Generate title if not set
        if (conversation.getTitle() == null || conversation.getTitle().isEmpty()) {
            conversation.setTitle(generateTitle(conversation, topic));
        }
        
        conversation = conversationRepository.save(conversation);
        log.info("Ended conversation {} with topic: {}", conversationId, topic);
        
        return conversation;
    }

    /**
     * Extract topic from conversation using AI
     */
    private String extractTopicFromConversation(Conversation conversation) {
        List<ChatMessage> messages = chatMessageRepository
            .findByConversationOrderByCreatedAtAsc(conversation);
        
        if (messages.isEmpty()) {
            return "General Discussion";
        }
        
        // Build context from all messages
        String conversationText = messages.stream()
            .map(m -> "User: " + m.getUserMessage() + "\nAI: " + m.getBotResponse())
            .collect(Collectors.joining("\n\n"));
        
        // Ask AI to extract the main topic
        String prompt = String.format(
            "Based on the following conversation, identify the main technology topic discussed. " +
            "Respond with ONLY the topic name (1-3 words, e.g., 'React', 'Docker', 'Spring Boot').\n\n" +
            "Conversation:\n%s\n\nMain topic:",
            conversationText.length() > 2000 ? conversationText.substring(0, 2000) + "..." : conversationText
        );
        
        try {
            String topic = ollamaService.generateResponse(prompt, "");
            // Clean up the response
            topic = topic.trim().replace("\"", "").replace(".", "");
            if (topic.length() > 50) {
                topic = topic.substring(0, 50);
            }
            return topic;
        } catch (Exception e) {
            log.error("Error extracting topic: ", e);
            return "Technology Discussion";
        }
    }

    /**
     * Generate a conversation title
     */
    private String generateTitle(Conversation conversation, String topic) {
        if (conversation.getMessageCount() == 1) {
            return topic + " Question";
        } else if (conversation.getMessageCount() <= 3) {
            return "Learning " + topic;
        } else {
            return topic + " Deep Dive";
        }
    }

    /**
     * Get all conversations for a user
     */
    public List<Conversation> getUserConversations(User user) {
        return conversationRepository.findTop20ByUserOrderByStartedAtDesc(user);
    }

    /**
     * Get active conversations for a user
     */
    public List<Conversation> getActiveConversations(User user) {
        return conversationRepository.findByUserAndStatusOrderByStartedAtDesc(user, ConversationStatus.ACTIVE);
    }

    /**
     * Get completed conversations for a user
     */
    public List<Conversation> getCompletedConversations(User user) {
        return conversationRepository.findByUserAndStatusOrderByStartedAtDesc(user, ConversationStatus.COMPLETED);
    }

    /**
     * Get a conversation by ID
     */
    public Conversation getConversation(Long conversationId) {
        return conversationRepository.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    /**
     * Archive a conversation
     */
    @Transactional
    public void archiveConversation(Long conversationId) {
        Conversation conversation = getConversation(conversationId);
        conversation.setStatus(ConversationStatus.ARCHIVED);
        conversationRepository.save(conversation);
        log.info("Archived conversation {}", conversationId);
    }

    /**
     * Delete a conversation and all its messages
     */
    @Transactional
    public void deleteConversation(Long conversationId) {
        Conversation conversation = getConversation(conversationId);
        
        // Delete all messages in this conversation
        List<ChatMessage> messages = chatMessageRepository.findByConversationOrderByCreatedAtAsc(conversation);
        chatMessageRepository.deleteAll(messages);
        
        // Delete the conversation
        conversationRepository.delete(conversation);
        log.info("Deleted conversation {} with {} messages", conversationId, messages.size());
    }
}
