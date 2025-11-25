package com.company.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Achievement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private String iconUrl;
    
    @Column(nullable = false)
    private Integer pointsReward;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AchievementType type;
    
    @Column(nullable = false)
    private Integer targetValue;
    
    @OneToMany(mappedBy = "achievement", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserAchievement> userAchievements = new ArrayList<>();
    
    public enum AchievementType {
        MESSAGES_SENT,
        CONSECUTIVE_DAYS,
        POINTS_EARNED,
        TOPICS_EXPLORED,
        LEVEL_REACHED
    }
}
