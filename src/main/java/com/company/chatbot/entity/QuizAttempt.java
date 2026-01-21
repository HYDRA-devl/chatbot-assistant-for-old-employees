package com.company.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"quiz", "user", "hibernateLazyInitializer", "handler"})
public class QuizAttempt {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(columnDefinition = "TEXT")
    private String answers;  // JSON format: {"1":"A","2":"B","3":"C"}
    
    private Integer score;  // Number of correct answers
    
    @Column(name = "total_questions")
    private Integer totalQuestions;
    
    @Column(name = "points_earned")
    private Integer pointsEarned = 0;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt = LocalDateTime.now();
}
