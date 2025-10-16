package com.example.leetnote_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class TogetherAiService {
    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public TogetherAiService(@Value("${together.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.together.xyz/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public String callTogetherModel(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "meta-llama/Llama-3.2-3B-Instruct-Turbo",
                    "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            String response = webClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parse the API response to extract the actual message content
            Map<String, Object> responseMap = mapper.readValue(response, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");

            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> firstChoice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) firstChoice.get("message");
                if (message != null) {
                    String content = (String) message.get("content");
                    System.out.println("\n\n🔹 Raw model output: " + content);
                    return content;
                }
            }

            throw new RuntimeException("Invalid response format from Together API");
        } catch (Exception e) {
            throw new RuntimeException("Error calling Together API: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> parseResponse(String rawOutput) {
        try {
            String cleaned = rawOutput.strip();

            // Keep only the part between the first "{" and the last "}"
            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}") + 1;
            if (start != -1 && end != -1) {
                cleaned = cleaned.substring(start, end);
            }

            Map<String, Object> parsed = mapper.readValue(cleaned, Map.class);
            System.out.println("✅ Parsed JSON: " + parsed);
            return parsed;
        } catch (Exception e) {
            System.err.println("❌ JSON parsing failed: " + e.getMessage());
            System.err.println("Raw output: " + rawOutput);
            return Map.of(
                    "rating", 1,
                    "issue", List.of("Invalid JSON"),
                    "feedback", List.of("Please try again.")
            );
        }
    }
}
