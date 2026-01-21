package com.company.chatbot.repository;

import com.company.chatbot.entity.QuizAttempt;
import com.company.chatbot.entity.Quiz;
import com.company.chatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    
    List<QuizAttempt> findByUserOrderByCompletedAtDesc(User user);
    
    List<QuizAttempt> findByQuizOrderByCompletedAtDesc(Quiz quiz);
    
    Optional<QuizAttempt> findByQuizAndUser(Quiz quiz, User user);
}
