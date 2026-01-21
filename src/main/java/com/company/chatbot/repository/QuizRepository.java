package com.company.chatbot.repository;

import com.company.chatbot.entity.Quiz;
import com.company.chatbot.entity.Conversation;
import com.company.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    
    Optional<Quiz> findByConversation(Conversation conversation);
    
    List<Quiz> findByUserOrderByCreatedAtDesc(User user);
    
    List<Quiz> findByUserAndTopicOrderByCreatedAtDesc(User user, String topic);
}
