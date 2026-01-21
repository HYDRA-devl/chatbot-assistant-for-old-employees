package com.company.chatbot.controller;

import com.company.chatbot.entity.Quiz;
import com.company.chatbot.entity.QuizAttempt;
import com.company.chatbot.entity.QuizQuestion;
import com.company.chatbot.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quiz")
@RequiredArgsConstructor
@Slf4j
public class QuizController {

    private final QuizService quizService;

    @PostMapping("/generate/{conversationId}")
    public ResponseEntity<Quiz> generateQuiz(@PathVariable Long conversationId) {
        log.info("Generating quiz for conversation: {}", conversationId);
        try {
            Quiz quiz = quizService.generateQuizFromConversation(conversationId);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            log.error("Error generating quiz: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Quiz> getQuizByConversation(@PathVariable Long conversationId) {
        try {
            return quizService.getQuizForConversation(conversationId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error getting quiz by conversation: ", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<Quiz> getQuiz(@PathVariable Long quizId) {
        try {
            Quiz quiz = quizService.getQuiz(quizId);
            return ResponseEntity.ok(quiz);
        } catch (Exception e) {
            log.error("Error getting quiz: ", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<QuizQuestion>> getQuizQuestions(@PathVariable Long quizId) {
        try {
            List<QuizQuestion> questions = quizService.getQuizQuestions(quizId);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("Error getting quiz questions: ", e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{quizId}/submit")
    public ResponseEntity<QuizAttempt> submitQuiz(
            @PathVariable Long quizId,
            @RequestBody SubmitQuizRequest request) {
        log.info("Submitting quiz {} for user {}", quizId, request.getUserId());
        try {
            QuizAttempt attempt = quizService.submitQuizAttempt(
                quizId,
                request.getUserId(),
                request.getAnswers()
            );
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            log.error("Error submitting quiz: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<QuizAttempt> getQuizAttempt(@PathVariable Long attemptId) {
        try {
            QuizAttempt attempt = quizService.getQuizAttempt(attemptId);
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            log.error("Error getting quiz attempt: ", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}/attempts")
    public ResponseEntity<List<QuizAttempt>> getUserQuizAttempts(@PathVariable Long userId) {
        try {
            List<QuizAttempt> attempts = quizService.getUserQuizAttempts(userId);
            return ResponseEntity.ok(attempts);
        } catch (Exception e) {
            log.error("Error getting user quiz attempts: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    // DTO for submit quiz request
    public static class SubmitQuizRequest {
        private Long userId;
        private Map<Long, String> answers; // questionId -> answer (A/B/C/D)

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Map<Long, String> getAnswers() {
            return answers;
        }

        public void setAnswers(Map<Long, String> answers) {
            this.answers = answers;
        }
    }
}
