package com.company.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"quiz", "hibernateLazyInitializer", "handler"})
public class QuizQuestion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;
    
    @Column(name = "option_a", nullable = false, columnDefinition = "TEXT")
    private String optionA;
    
    @Column(name = "option_b", nullable = false, columnDefinition = "TEXT")
    private String optionB;
    
    @Column(name = "option_c", nullable = false, columnDefinition = "TEXT")
    private String optionC;
    
    @Column(name = "option_d", nullable = false, columnDefinition = "TEXT")
    private String optionD;
    
    @Column(name = "correct_answer", nullable = false, length = 1)
    private String correctAnswer;  // 'A', 'B', 'C', or 'D'
    
    @Column(columnDefinition = "TEXT")
    private String explanation;
    
    @Column(name = "question_order")
    private Integer questionOrder;
}
