package com.company.chatbot.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SkillMapResponse(
    List<SkillCatalogItem> catalog,
    List<SkillMappingItem> mappings,
    LocalDateTime updatedAt,
    String source
) {}
