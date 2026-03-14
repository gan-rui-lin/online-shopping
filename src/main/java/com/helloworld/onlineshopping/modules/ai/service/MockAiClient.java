package com.helloworld.onlineshopping.modules.ai.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class MockAiClient implements AiClient {

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (systemPrompt.contains("title")) {
            return "1. \"Premium Quality " + extractKeyword(userMessage) + " - Unmatched Performance\"\n" +
                   "2. \"Next-Gen " + extractKeyword(userMessage) + " | Redefining Excellence\"\n" +
                   "3. \"" + extractKeyword(userMessage) + " Pro Series - Built for Perfection\"";
        } else if (systemPrompt.contains("selling")) {
            return "1. Superior build quality with premium materials\n" +
                   "2. Industry-leading performance benchmarks\n" +
                   "3. Ergonomic design for maximum comfort\n" +
                   "4. Outstanding value for money in its category\n" +
                   "5. Backed by comprehensive warranty and support";
        } else if (systemPrompt.contains("review") || systemPrompt.contains("Review")) {
            return "PROS: Excellent quality, Great value, Fast delivery, Good packaging\n" +
                   "CONS: Limited color options, Could improve documentation\n" +
                   "SUMMARY: Overall highly rated product with strong customer satisfaction. Most buyers praise the quality and value proposition.";
        } else {
            return "Discover the extraordinary " + extractKeyword(userMessage) + " — where innovation meets elegance. " +
                   "Crafted for those who demand the best, this product delivers outstanding performance with a refined touch. " +
                   "Whether you're a professional or an enthusiast, you'll appreciate the attention to detail and superior craftsmanship.";
        }
    }

    private String extractKeyword(String text) {
        if (text == null || text.isEmpty()) return "Product";
        String[] words = text.split("[\\s,]+");
        return words.length > 0 ? words[0] : "Product";
    }
}
