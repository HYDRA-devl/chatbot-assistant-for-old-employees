package com.company.chatbot.service;

import com.company.chatbot.dto.SkillCatalogItem;
import com.company.chatbot.dto.SkillMapResponse;
import com.company.chatbot.dto.SkillMappingItem;
import com.company.chatbot.entity.ChatMessage;
import com.company.chatbot.entity.User;
import com.company.chatbot.repository.ChatMessageRepository;
import com.company.chatbot.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillMappingService {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final OllamaService ollamaService;
    private final ObjectMapper objectMapper;

    private final Map<Long, SkillMapResponse> cache = new ConcurrentHashMap<>();

    private static final List<SkillDefinition> CATALOG = List.of(
        new SkillDefinition("react-basics", "React Basics", "Frontend", "Components, props, state, JSX", List.of("react", "jsx", "component", "hook")),
        new SkillDefinition("spring-boot", "Spring Boot", "Backend", "APIs, controllers, services", List.of("spring", "boot", "controller", "service", "endpoint")),
        new SkillDefinition("rest-api", "REST APIs", "Backend", "HTTP methods, status codes, JSON", List.of("rest", "api", "http", "json", "endpoint")),
        new SkillDefinition("sql-basics", "SQL Basics", "Data", "Queries, joins, data models", List.of("sql", "query", "join", "database", "postgres")),
        new SkillDefinition("devops-docker", "Docker", "DevOps", "Images, containers, compose", List.of("docker", "container", "image", "compose")),
        new SkillDefinition("git-workflow", "Git Workflow", "Engineering", "Branching, commits, PRs", List.of("git", "branch", "commit", "merge", "pull request")),
        new SkillDefinition("cloud-basics", "Cloud Basics", "Cloud", "Deployment, hosting, environments", List.of("cloud", "aws", "azure", "gcp", "deploy")),
        new SkillDefinition("testing-basics", "Testing Basics", "Quality", "Unit tests, integration tests", List.of("test", "unit test", "integration", "junit"))
    );

    public SkillMapResponse getSkillMap(Long userId, boolean refresh) {
        if (!refresh) {
            SkillMapResponse cached = cache.get(userId);
            if (cached != null) {
                return cached;
            }
            return buildRuleBasedMap(userId);
        }

        return buildLlmMap(userId);
    }

    private SkillMapResponse buildLlmMap(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatMessage> recent = chatMessageRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        String context = recent.stream()
            .map(this::formatMessage)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("\n"));

        List<SkillCatalogItem> catalog = CATALOG.stream()
            .map(def -> new SkillCatalogItem(def.id, def.name, def.category, def.description))
            .toList();

        List<SkillMappingItem> mappings = tryLlmMapping(catalog, context)
            .orElseGet(() -> fallbackMapping(catalog, context));

        String source = mappings.stream().anyMatch(item -> "LLM".equalsIgnoreCase(item.source())) ? "LLM" : "Rule";
        SkillMapResponse response = new SkillMapResponse(catalog, mappings, LocalDateTime.now(), source);

        if ("LLM".equalsIgnoreCase(source)) {
            cache.put(userId, response);
        }

        return response;
    }

    private SkillMapResponse buildRuleBasedMap(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<ChatMessage> recent = chatMessageRepository.findTop10ByUserOrderByCreatedAtDesc(user);
        String context = recent.stream()
            .map(this::formatMessage)
            .filter(Objects::nonNull)
            .collect(Collectors.joining("\n"));

        List<SkillCatalogItem> catalog = CATALOG.stream()
            .map(def -> new SkillCatalogItem(def.id, def.name, def.category, def.description))
            .toList();

        List<SkillMappingItem> mappings = fallbackMapping(catalog, context);
        return new SkillMapResponse(catalog, mappings, LocalDateTime.now(), "Rule");
    }

    private String formatMessage(ChatMessage message) {
        if (message == null) {
            return null;
        }
        String userText = Optional.ofNullable(message.getUserMessage()).orElse("");
        String botText = Optional.ofNullable(message.getBotResponse()).orElse("");
        if (userText.isBlank() && botText.isBlank()) {
            return null;
        }
        return "User: " + userText + "\nAssistant: " + botText;
    }

    private Optional<List<SkillMappingItem>> tryLlmMapping(List<SkillCatalogItem> catalog, String context) {
        if (context == null || context.isBlank()) {
            return Optional.empty();
        }

        try {
            String prompt = buildPrompt(catalog, context);
            String response = ollamaService.generateResponse(prompt, "", 800, false);
            String json = extractJson(response);
            if (json == null) {
                return Optional.empty();
            }
            JsonNode root = objectMapper.readTree(json);
            JsonNode mappingsNode = root.has("mappings") ? root.get("mappings") : root;
            List<LlmMapping> llmMappings = objectMapper.convertValue(mappingsNode, new TypeReference<>() {});

            Map<String, SkillCatalogItem> catalogById = catalog.stream()
                .collect(Collectors.toMap(SkillCatalogItem::id, item -> item));

            List<SkillMappingItem> items = new ArrayList<>();
            for (LlmMapping mapping : llmMappings) {
                SkillCatalogItem item = catalogById.get(mapping.skillId);
                if (item == null) {
                    continue;
                }
                items.add(new SkillMappingItem(
                    item.id(),
                    item.name(),
                    item.category(),
                    normalizeLevel(mapping.level),
                    clamp(mapping.confidence),
                    mapping.evidence,
                    "LLM"
                ));
            }

            if (items.isEmpty()) {
                return Optional.empty();
            }

            List<SkillMappingItem> merged = mergeWithCatalog(catalog, items, "LLM");
            return Optional.of(merged);
        } catch (Exception e) {
            log.warn("LLM skill mapping failed, falling back to rule-based mapping", e);
            return Optional.empty();
        }
    }

    private List<SkillMappingItem> fallbackMapping(List<SkillCatalogItem> catalog, String context) {
        String lower = context == null ? "" : context.toLowerCase(Locale.ROOT);
        List<SkillMappingItem> mapped = new ArrayList<>();
        for (SkillDefinition def : CATALOG) {
            boolean matched = def.keywords.stream().anyMatch(lower::contains);
            String level = matched ? "Beginner" : "Not started";
            int confidence = matched ? 45 : 0;
            String evidence = matched ? "Matched recent learning topics" : "No recent evidence";
            mapped.add(new SkillMappingItem(
                def.id,
                def.name,
                def.category,
                level,
                confidence,
                evidence,
                "Rule"
            ));
        }
        return mapped;
    }

    private List<SkillMappingItem> mergeWithCatalog(List<SkillCatalogItem> catalog, List<SkillMappingItem> mapped, String source) {
        Map<String, SkillMappingItem> byId = mapped.stream()
            .collect(Collectors.toMap(SkillMappingItem::skillId, item -> item));
        List<SkillMappingItem> merged = new ArrayList<>();
        for (SkillCatalogItem item : catalog) {
            SkillMappingItem mapping = byId.get(item.id());
            if (mapping != null) {
                merged.add(mapping);
            } else {
                merged.add(new SkillMappingItem(
                    item.id(),
                    item.name(),
                    item.category(),
                    "Not started",
                    0,
                    "No evidence yet",
                    source
                ));
            }
        }
        return merged;
    }

    private String buildPrompt(List<SkillCatalogItem> catalog, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a skills analyst. Map the employee's recent learning activity to a predefined skill catalog.\n");
        sb.append("Return JSON only. Use this format: {\"mappings\":[{\"skillId\":...,\"level\":...,\"confidence\":0-100,\"evidence\":...}]}\n");
        sb.append("Levels: Not started, Beginner, Intermediate, Advanced.\n");
        sb.append("Catalog:\n");
        for (SkillCatalogItem item : catalog) {
            sb.append("- ").append(item.id()).append(" | ")
                .append(item.name()).append(" | ")
                .append(item.description()).append("\n");
        }
        sb.append("\nRecent learning activity:\n");
        sb.append(context);
        sb.append("\nOnly include skills from the catalog.\n");
        return sb.toString();
    }

    private String extractJson(String response) {
        if (response == null) {
            return null;
        }
        int first = response.indexOf('{');
        int last = response.lastIndexOf('}');
        if (first < 0 || last <= first) {
            return null;
        }
        return response.substring(first, last + 1);
    }

    private String normalizeLevel(String level) {
        if (level == null) {
            return "Not started";
        }
        String normalized = level.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "beginner" -> "Beginner";
            case "intermediate" -> "Intermediate";
            case "advanced" -> "Advanced";
            default -> "Not started";
        };
    }

    private int clamp(Integer value) {
        if (value == null) {
            return 0;
        }
        return Math.max(0, Math.min(100, value));
    }

    private record SkillDefinition(
        String id,
        String name,
        String category,
        String description,
        List<String> keywords
    ) {}

    private record LlmMapping(
        String skillId,
        String level,
        Integer confidence,
        String evidence
    ) {}
}
