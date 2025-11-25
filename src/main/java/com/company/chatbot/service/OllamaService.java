package com.company.chatbot.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OllamaService {

    private final WebClient webClient;
    private final String model;
    private final long timeout;

    public OllamaService(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model}") String model,
            @Value("${ollama.timeout}") long timeout) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.model = model;
        this.timeout = timeout;
        log.info("Ollama service initialized with base URL: {} and model: {}", baseUrl, model);
    }

    public String generateResponse(String prompt, String context) {
        String enhancedPrompt = buildPrompt(prompt, context);
        
        OllamaRequest request = new OllamaRequest();
        request.setModel(model);
        request.setPrompt(enhancedPrompt);
        request.setStream(false);

        try {
            log.info("Sending request to Ollama with prompt length: {} characters", enhancedPrompt.length());
            
            OllamaResponse response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(OllamaResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .block();

            if (response != null && response.getResponse() != null) {
                log.info("Received response from Ollama: {} characters", response.getResponse().length());
                return response.getResponse();
            } else {
                log.error("Received null response from Ollama");
                return "I apologize, but I couldn't generate a response at this moment. Please try again.";
            }
        } catch (Exception e) {
            log.error("Error calling Ollama API: ", e);
            return "I apologize, but I encountered an error while processing your request. Please check if Ollama is running and try again.";
        }
    }

    private String buildPrompt(String userMessage, String context) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a helpful AI assistant for a company, helping employees stay updated with technology advancements. ");
        
        if (context != null && !context.isEmpty()) {
            prompt.append("\n\nContext from company documents:\n");
            prompt.append(context);
            prompt.append("\n\n");
        }
        
        prompt.append("User question: ").append(userMessage);
        prompt.append("\n\nProvide a clear, concise, and helpful response.");
        
        return prompt.toString();
    }

    @Data
    public static class OllamaRequest {
        private String model;
        private String prompt;
        private boolean stream;
        private Map<String, Object> options;
    }

    @Data
    public static class OllamaResponse {
        private String model;
        private String created_at;
        private String response;
        private boolean done;
    }
}
