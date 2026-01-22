package com.company.chatbot.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class GeminiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final long timeout;

    public GeminiService(
            @Value("${gemini.api-key:}") String apiKey,
            @Value("${gemini.model:gemini-2.5-flash}") String model,
            @Value("${gemini.timeout:20000}") long timeout) {
        this.webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .defaultHeader("x-goog-api-key", apiKey == null ? "" : apiKey)
            .build();
        this.apiKey = apiKey;
        this.model = model;
        this.timeout = timeout;
        log.info("Gemini service initialized with model: {}", model);
    }

    public String generateResponse(String prompt, String context, int maxTokens) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Gemini API key is not configured.";
        }

        String effectivePrompt = prompt;
        if (context != null && !context.isBlank()) {
            effectivePrompt = "Context:\n" + context + "\n\nTask:\n" + prompt;
        }

        GeminiRequest request = new GeminiRequest();
        request.setContents(List.of(new Content(List.of(new Part(effectivePrompt)))));
        request.setGenerationConfig(new GenerationConfig(maxTokens, 0.7, 0.9));

        try {
            GeminiResponse response = webClient.post()
                .uri("/v1beta/models/{model}:generateContent", model)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .block();

            if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
                return "";
            }

            Candidate candidate = response.getCandidates().get(0);
            if (candidate == null || candidate.getContent() == null || candidate.getContent().getParts() == null) {
                return "";
            }
            return candidate.getContent().getParts().stream()
                .map(Part::getText)
                .filter(text -> text != null && !text.isBlank())
                .findFirst()
                .orElse("");
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            return "";
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiRequest {
        private List<Content> contents;
        private GenerationConfig generationConfig;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GenerationConfig {
        private int maxOutputTokens;
        private double temperature;
        private double topP;

        public GenerationConfig(int maxOutputTokens, double temperature, double topP) {
            this.maxOutputTokens = maxOutputTokens;
            this.temperature = temperature;
            this.topP = topP;
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;

        public Part(String text) {
            this.text = text;
        }
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
    }
}
