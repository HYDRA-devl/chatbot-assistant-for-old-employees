package com.company.chatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmRouterService {

    private final OllamaService ollamaService;
    private final GeminiService geminiService;

    @Value("${llm.provider:ollama}")
    private String provider;

    public String generateResponse(String prompt, String context, int maxTokens, boolean wrapPrompt) {
        String selected = provider == null ? "ollama" : provider.trim().toLowerCase();
        return switch (selected) {
            case "gemini" -> geminiService.generateResponse(prompt, context, maxTokens);
            default -> ollamaService.generateResponse(prompt, context, maxTokens, wrapPrompt);
        };
    }
}
