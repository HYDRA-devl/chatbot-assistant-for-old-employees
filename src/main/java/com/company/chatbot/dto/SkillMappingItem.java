package com.company.chatbot.dto;

public record SkillMappingItem(
    String skillId,
    String name,
    String category,
    String level,
    int confidence,
    String evidence,
    String source
) {}
