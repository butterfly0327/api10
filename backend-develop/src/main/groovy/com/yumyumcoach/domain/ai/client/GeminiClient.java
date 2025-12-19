package com.yumyumcoach.domain.ai.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class GeminiClient {

    private static final String DEFAULT_MODEL = "models/gemini-1.5-flash";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GeminiClient(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key:}") String apiKey
    ) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.restClient = restClientBuilder
                .baseUrl("https://generativelanguage.googleapis.com")
                .build();
    }

    public String generateText(String prompt) {
        return generateText(prompt, Collections.emptyMap());
    }

    public String generateText(String prompt, Map<String, Object> systemInstruction) {
        if (apiKey == null || apiKey.isBlank()) {
            return "[SKIPPED: Gemini API key not configured]";
        }

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                )),
                "system_instruction", systemInstruction
        );

        try {
            String rawResponse = restClient.post()
                    .uri("/v1beta/{model}:generateContent?key={key}", DEFAULT_MODEL, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);
            return extractText(rawResponse);
        } catch (RestClientResponseException ex) {
            return "[ERROR] " + ex.getMessage();
        }
    }

    private String extractText(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return "";
        }
        try {
            GeminiResponse response = objectMapper.readValue(rawResponse, GeminiResponse.class);
            return response.firstText();
        } catch (Exception ex) {
            return rawResponse;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiResponse {
        @JsonProperty("candidates")
        private List<GeminiCandidate> candidates = new ArrayList<>();

        public String firstText() {
            if (candidates == null || candidates.isEmpty()) {
                return "";
            }
            return candidates.get(0).text();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiCandidate {
        @JsonProperty("content")
        private GeminiContent content;

        public String text() {
            if (content == null) {
                return "";
            }
            return content.text();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiContent {
        @JsonProperty("parts")
        private List<GeminiPart> parts = new ArrayList<>();

        public String text() {
            if (parts == null || parts.isEmpty()) {
                return "";
            }
            return parts.get(0).text();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiPart {
        @JsonProperty("text")
        private String text;

        public String text() {
            return text == null ? "" : text;
        }
    }
}
