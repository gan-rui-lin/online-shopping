package com.helloworld.onlineshopping.modules.rag.service;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
@Service("ragMockAiClient")
@Primary
public class MockAiClient implements AiClient {
    @Override
    public String chat(String systemPrompt, String userMessage) {
        String context = systemPrompt.length() > 200 ? systemPrompt.substring(0, 200) : systemPrompt;
        return "Based on the product information available, here is my answer to your question:\n\n" +
            "Regarding \"" + userMessage + "\": This product offers excellent quality and value. " +
            "The specifications meet most users' needs, and customer feedback has been very positive. " +
            "I'd recommend checking the detailed product description for specific technical parameters.\n\n" +
            "Is there anything else you'd like to know?";
    }
}
