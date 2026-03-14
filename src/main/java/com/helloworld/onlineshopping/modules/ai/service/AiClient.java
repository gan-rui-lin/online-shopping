package com.helloworld.onlineshopping.modules.ai.service;

public interface AiClient {
    String chat(String systemPrompt, String userMessage);
}
