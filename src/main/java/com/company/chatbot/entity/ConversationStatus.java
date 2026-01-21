package com.company.chatbot.entity;

public enum ConversationStatus {
    ACTIVE,      // Conversation is ongoing
    COMPLETED,   // Conversation ended, quiz can be generated
    ARCHIVED     // User archived the conversation
}
