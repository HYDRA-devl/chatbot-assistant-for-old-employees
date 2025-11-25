package com.company.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class UserAchievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-achievements")
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "achievement_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "userAchievements"})
    private Achievement achievement;
    
    @Column(name = "earned_at")
    private LocalDateTime earnedAt = LocalDateTime.now();
    
    @Column(name = "progress")
    private Integer progress = 0;
    
    @Column(name = "completed")
    private Boolean completed = false;
}
