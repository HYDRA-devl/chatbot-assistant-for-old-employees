package com.company.chatbot.repository;

import com.company.chatbot.entity.QuizQuestion;
import com.company.chatbot.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    
    List<QuizQuestion> findByQuizOrderByQuestionOrderAsc(Quiz quiz);
    
    List<QuizQuestion> findByQuizIdOrderByQuestionOrderAsc(Long quizId);
}
