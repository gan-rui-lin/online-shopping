package com.helloworld.onlineshopping.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AiJsonExtractor {
    private static final Pattern CODE_BLOCK = Pattern.compile("(?s)^```(?:json)?\\s*(.*?)\\s*```$");

    private AiJsonExtractor() {
    }

    public static String unwrapJson(String text) {
        if (text == null) {
            return "";
        }
        String cleaned = text.trim();
        Matcher matcher = CODE_BLOCK.matcher(cleaned);
        if (matcher.find()) {
            cleaned = matcher.group(1).trim();
        }

        int arrayStart = cleaned.indexOf('[');
        int objectStart = cleaned.indexOf('{');
        int start;
        if (arrayStart < 0) {
            start = objectStart;
        } else if (objectStart < 0) {
            start = arrayStart;
        } else {
            start = Math.min(arrayStart, objectStart);
        }
        if (start < 0) {
            return cleaned;
        }

        char startChar = cleaned.charAt(start);
        char endChar = startChar == '[' ? ']' : '}';
        int end = cleaned.lastIndexOf(endChar);
        if (end > start) {
            return cleaned.substring(start, end + 1).trim();
        }
        return cleaned.substring(start).trim();
    }
}
