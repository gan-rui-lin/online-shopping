package com.helloworld.onlineshopping.modules.rag.service;
public interface AiClient {
    String chat(String systemPrompt, String userMessage);
}
