package com.company.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Groq Cloud API Service - Ultra-fast LLM inference
 * Free tier: 30 requests/minute, 6000 tokens/minute
 * Speed: 300-800 tokens/second (vs Ollama's 10-30 tokens/second)
 */
@Service
@Slf4j
public class GroqService {

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;

    public GroqService(
            @Value("${groq.api-key:}") String apiKey,
            @Value("${groq.model:llama-3.3-70b-versatile}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = new ObjectMapper();
        this.webClient = WebClient.builder()
                .baseUrl("https://api.groq.com/openai/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        log.info("Groq service initialized with model: {}", model);
    }

    public String generateResponse(String prompt, String context) {
        String systemMessage = "You are an AI assistant helping employees learn technology. " +
                "Be concise, practical, and accurate.";
        
        String userMessage = context != null && !context.isEmpty() 
            ? "Context: " + context + "\n\nQuestion: " + prompt
            : prompt;

        GroqRequest request = new GroqRequest();
        request.setModel(model);
        request.setMessages(List.of(
            new Message("system", systemMessage),
            new Message("user", userMessage)
        ));
        request.setTemperature(0.7);
        request.setMax_tokens(1024);

        try {
            log.info("Sending request to Groq API");
            long startTime = System.currentTimeMillis();
            
            GroqResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GroqResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Groq response received in {}ms", duration);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            } else {
                log.error("Invalid response from Groq API");
                return "I apologize, but I couldn't generate a response. Please try again.";
            }
        } catch (Exception e) {
            log.error("Error calling Groq API: ", e);
            return "I apologize, but I encountered an error. Please try again.";
        }
    }

    /**
     * Generate quiz questions using Groq
     */
    public String generateQuiz(String topic, int numQuestions) {
        String prompt = String.format("""
            Generate %d multiple choice quiz questions about %s.
            
            Format as JSON:
            {
              "questions": [
                {
                  "question": "Question text?",
                  "options": ["A) Option 1", "B) Option 2", "C) Option 3", "D) Option 4"],
                  "correctAnswer": "A",
                  "explanation": "Brief explanation"
                }
              ]
            }
            
            Make questions practical for employees learning technology.
            """, numQuestions, topic);

        GroqRequest request = new GroqRequest();
        request.setModel(model);
        request.setMessages(List.of(
            new Message("system", "You are a quiz generator. Always respond with valid JSON only."),
            new Message("user", prompt)
        ));
        request.setTemperature(0.5);
        request.setMax_tokens(2000);

        try {
            GroqResponse response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GroqResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
            return null;
        } catch (Exception e) {
            log.error("Error generating quiz with Groq: ", e);
            return null;
        }
    }

    @Data
    public static class GroqRequest {
        private String model;
        private List<Message> messages;
        private Double temperature;
        private Integer max_tokens;
    }

    @Data
    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    public static class GroqResponse {
        private List<Choice> choices;
        private Usage usage;
    }

    @Data
    public static class Choice {
        private Message message;
        private Integer index;
    }

    @Data
    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
    }
}
