package com.company.chatbot.repository;

import com.company.chatbot.entity.Conversation;
import com.company.chatbot.entity.ConversationStatus;
import com.company.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    List<Conversation> findByUserOrderByStartedAtDesc(User user);
    
    List<Conversation> findByUserAndStatusOrderByStartedAtDesc(User user, ConversationStatus status);
    
    Optional<Conversation> findByUserAndStatus(User user, ConversationStatus status);
    
    List<Conversation> findTop20ByUserOrderByStartedAtDesc(User user);
}
