package com.yumyumcoach.domain.aiadvisor.gemini;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yumyumcoach.global.exception.BusinessException;
import com.yumyumcoach.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GeminiClient {

    private final String apiKey;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiClient(
            @Value("${gemini.api.key:}") String apiKey,
            ObjectMapper objectMapper
    ) {
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
    }

    public String generateContent(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Gemini API key가 설정되지 않았습니다.");
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", prompt))
                ))
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Gemini API 호출에 실패했습니다.");
        }

        try {
            Map<String, Object> parsed = objectMapper.readValue(response.getBody(), new TypeReference<Map<String, Object>>() {
            });
            return extractText(parsed);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Gemini 응답 파싱에 실패했습니다.");
        }
    }

    private String extractText(Map<String, Object> parsed) {
        Object candidatesObj = parsed.get("candidates");
        if (candidatesObj instanceof List<?> candidates && !candidates.isEmpty()) {
            Object first = candidates.get(0);
            if (first instanceof Map<?, ?> candidate) {
                Object content = candidate.get("content");
                if (content instanceof Map<?, ?> contentMap) {
                    Object parts = contentMap.get("parts");
                    if (parts instanceof List<?> partsList && !partsList.isEmpty()) {
                        Object firstPart = partsList.get(0);
                        if (firstPart instanceof Map<?, ?> partMap) {
                            Object text = partMap.get("text");
                            if (text != null) {
                                return text.toString();
                            }
                        }
                    }
                }
            }
        }
        Object text = parsed.get("text");
        if (text != null) {
            return text.toString();
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "Gemini 응답에 텍스트가 없습니다.");
    }
}
