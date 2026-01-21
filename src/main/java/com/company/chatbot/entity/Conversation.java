package com.company.chatbot.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
public class Conversation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "user_id", insertable = false, updatable = false)
    private Long userId;  // For JSON serialization without loading User
    
    @Column(length = 255)
    private String title;
    
    @Column(length = 100)
    private String topic;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ConversationStatus status = ConversationStatus.ACTIVE;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt = LocalDateTime.now();
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "message_count")
    private Integer messageCount = 0;
    
    // Helper method to increment message count
    public void incrementMessageCount() {
        this.messageCount = (this.messageCount == null ? 0 : this.messageCount) + 1;
    }
}
