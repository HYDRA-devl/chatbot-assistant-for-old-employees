package com.company.chatbot.repository;

import com.company.chatbot.entity.ChatMessage;
import com.company.chatbot.entity.Conversation;
import com.company.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserOrderByCreatedAtDesc(User user);
    List<ChatMessage> findTop10ByUserOrderByCreatedAtDesc(User user);
    Long countByUser(User user);
    
    // Conversation-based queries
    List<ChatMessage> findByConversationOrderByCreatedAtAsc(Conversation conversation);
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
    Long countByConversation(Conversation conversation);
}
