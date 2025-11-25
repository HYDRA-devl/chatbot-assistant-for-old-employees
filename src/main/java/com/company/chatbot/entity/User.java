package com.company.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String fullName;
    
    private String department;
    
    @Column(name = "total_points")
    private Integer totalPoints = 0;
    
    @Column(name = "level")
    private Integer level = 1;
    
    @Column(name = "messages_sent")
    private Integer messagesSent = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-messages")
    private List<ChatMessage> chatMessages = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference("user-achievements")
    private List<UserAchievement> achievements = new ArrayList<>();
}
