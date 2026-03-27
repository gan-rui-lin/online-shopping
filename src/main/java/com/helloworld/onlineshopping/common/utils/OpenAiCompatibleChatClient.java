package com.helloworld.onlineshopping.common.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.helloworld.onlineshopping.modules.ai.service.AiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class OpenAiCompatibleChatClient implements AiClient, com.helloworld.onlineshopping.modules.rag.service.AiClient {

    @Value("${app.ai.base-url:https://openrouter.ai/api/v1/chat/completions}")
    private String baseUrl;

    @Value("${app.ai.api-key:}")
    private String apiKey;

    @Value("${app.ai.model:deepseek/deepseek-chat-v3-0324:free}")
    private String model;

    @Value("${app.ai.timeout-ms:20000}")
    private Integer timeoutMs;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("AI API key is empty. Please set app.ai.api-key or disable app.ai.enabled.");
            return "AI service is enabled but API key is missing. Please configure app.ai.api-key.";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            headers.set("HTTP-Referer", "https://online-shopping.local");
            headers.set("X-Title", "online-shopping");

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            payload.put("temperature", 0.4);
            payload.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt == null ? "" : systemPrompt),
                Map.of("role", "user", "content", userMessage == null ? "" : userMessage)
            ));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("AI response status not successful: {}", response.getStatusCode());
                return "AI service is temporarily unavailable. Please try again later.";
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                return "AI returned an empty response. Please retry with more details.";
            }
            return contentNode.asText();
        } catch (Exception ex) {
            log.error("Call AI gateway failed", ex);
            return "AI service request failed. Please try again later.";
        }
    }
}