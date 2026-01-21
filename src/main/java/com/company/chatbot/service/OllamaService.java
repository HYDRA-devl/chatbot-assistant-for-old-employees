package com.company.chatbot.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
        return generateResponse(prompt, context, 512);  // Default limit for regular chat
    }

    /**
     * Generate response with custom token limit
     * @param prompt The user's prompt
     * @param context Additional context
     * @param maxTokens Maximum tokens to generate (use higher for structured output like JSON)
     * @return Generated response
     */
    public String generateResponse(String prompt, String context, int maxTokens) {
        return generateResponse(prompt, context, maxTokens, true);
    }

    /**
     * Generate response with custom token limit and optional prompt wrapping
     * @param prompt The user's prompt
     * @param context Additional context
     * @param maxTokens Maximum tokens to generate
     * @param wrapPrompt Whether to wrap prompt with AI assistant instructions
     * @return Generated response
     */
    public String generateResponse(String prompt, String context, int maxTokens, boolean wrapPrompt) {
        String enhancedPrompt = wrapPrompt ? buildPrompt(prompt, context) : prompt;

        OllamaRequest request = new OllamaRequest();
        request.setModel(model);
        request.setPrompt(enhancedPrompt);
        request.setStream(false);

        // Optimize for speed
        Map<String, Object> options = Map.of(
            "num_predict", maxTokens,     // Configurable response length
            "temperature", 0.7,           // Lower = more focused
            "top_k", 40,                  // Reduce sampling pool
            "top_p", 0.9,                 // Nucleus sampling
            "num_ctx", 2048               // Context window size
        );
        request.setOptions(options);

        try {
            log.info("Sending request to Ollama with prompt length: {} characters, max tokens: {}",
                    enhancedPrompt.length(), maxTokens);

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
        prompt.append("You are an AI assistant helping employees learn technology. Be concise and practical.\n\n");
        
        if (context != null && !context.isEmpty()) {
            // Limit context to 500 characters for speed
            String limitedContext = context.length() > 500 ? context.substring(0, 500) + "..." : context;
            prompt.append("Context: ").append(limitedContext).append("\n\n");
        }
        
        prompt.append("Question: ").append(userMessage);
        prompt.append("\n\nAnswer:");
        
        return prompt.toString();
    }

    @Data
    public static class OllamaRequest {
        private String model;
        private String prompt;
        private boolean stream;
        private Map<String, Object> options;
    }

    /**
     * Stream response from Ollama token by token
     * @param prompt User's question
     * @param context Additional context
     * @param onToken Callback for each token received
     */
    public void generateStreamingResponse(String prompt, String context, Consumer<String> onToken, Runnable onComplete) {
        String enhancedPrompt = buildPrompt(prompt, context);
        
        OllamaRequest request = new OllamaRequest();
        request.setModel(model);
        request.setPrompt(enhancedPrompt);
        request.setStream(true);  // Enable streaming
        
        Map<String, Object> options = Map.of(
            "num_predict", 512,
            "temperature", 0.7,
            "top_k", 40,
            "top_p", 0.9,
            "num_ctx", 2048
        );
        request.setOptions(options);

        try {
            log.info("Starting streaming request to Ollama");
            
            webClient.post()
                    .uri("/api/generate")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(OllamaStreamResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .subscribe(
                        response -> {
                            // Send each token to the callback - don't filter out spaces!
                            if (response.getResponse() != null) {
                                String token = response.getResponse();
                                log.debug("Received token from Ollama: '{}' (length: {})", token, token.length());
                                onToken.accept(token);
                            }
                            // Check if done
                            if (response.isDone()) {
                                log.info("Streaming completed");
                                onComplete.run();
                            }
                        },
                        error -> {
                            log.error("Error in streaming: ", error);
                            onToken.accept("[ERROR: " + error.getMessage() + "]");
                            onComplete.run();
                        }
                    );
        } catch (Exception e) {
            log.error("Error starting stream: ", e);
            onToken.accept("[ERROR: " + e.getMessage() + "]");
            onComplete.run();
        }
    }

    @Data
    public static class OllamaResponse {
        private String model;
        private String created_at;
        private String response;
        private boolean done;
    }
    
    @Data
    public static class OllamaStreamResponse {
        private String model;
        private String created_at;
        private String response;
        private boolean done;
    }
}
