package com.company.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response;
    private Long messageId;
    private Integer pointsEarned;
    private Long responseTimeMs;
}
