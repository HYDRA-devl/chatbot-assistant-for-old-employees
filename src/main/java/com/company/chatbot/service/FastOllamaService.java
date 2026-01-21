package com.company.chatbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * Fast Ollama service for quizzes and simple questions
 * Uses a smaller, faster model (phi3:mini or llama3.2:3b)
 */
@Service
@Slf4j
public class FastOllamaService {

    private final WebClient webClient;
    private final String fastModel = "phi3:mini"; // Fast model for quizzes
    private final long timeout;

    public FastOllamaService(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.timeout}") long timeout) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.timeout = timeout;
        log.info("Fast Ollama service initialized with model: {}", fastModel);
    }

    /**
     * Generate quiz questions based on a topic
     */
    public String generateQuiz(String topic, int numQuestions) {
        String prompt = buildQuizPrompt(topic, numQuestions);
        
        OllamaService.OllamaRequest request = new OllamaService.OllamaRequest();
        request.setModel(fastModel);
        request.setPrompt(prompt);
        request.setStream(false);
        
        // Optimize for speed
        Map<String, Object> options = Map.of(
            "num_predict", 800,        // Enough for quiz questions
            "temperature", 0.5,        // More deterministic for quizzes
            "top_k", 30,
            "top_p", 0.85
        );
        request.setOptions(options);

        try {
            log.info("Generating quiz for topic: {}", topic);
            
            OllamaService.OllamaResponse response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaService.OllamaResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response != null && response.getResponse() != null) {
                log.info("Quiz generated successfully");
                return response.getResponse();
            } else {
                log.error("Failed to generate quiz");
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating quiz: ", e);
            return null;
        }
    }

    /**
     * Quick answer for simple questions
     */
    public String quickAnswer(String question) {
        OllamaService.OllamaRequest request = new OllamaService.OllamaRequest();
        request.setModel(fastModel);
        request.setPrompt(question);
        request.setStream(false);
        
        Map<String, Object> options = Map.of(
            "num_predict", 256,        // Short answers
            "temperature", 0.6,
            "top_k", 40,
            "top_p", 0.9
        );
        request.setOptions(options);

        try {
            OllamaService.OllamaResponse response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaService.OllamaResponse.class)
                    .timeout(Duration.ofMillis(20000)) // 20 second timeout for quick answers
                    .block();

            return response != null ? response.getResponse() : null;
        } catch (Exception e) {
            log.error("Error getting quick answer: ", e);
            return null;
        }
    }

    private String buildQuizPrompt(String topic, int numQuestions) {
        return String.format("""
            Generate %d multiple choice quiz questions about %s.
            
            Format your response as JSON:
            {
              "questions": [
                {
                  "question": "Question text here?",
                  "options": ["A) Option 1", "B) Option 2", "C) Option 3", "D) Option 4"],
                  "correctAnswer": "A",
                  "explanation": "Brief explanation of why this is correct"
                }
              ]
            }
            
            Make questions practical and relevant for employees learning technology.
            Focus on understanding concepts, not memorization.
            """, numQuestions, topic);
    }
}
