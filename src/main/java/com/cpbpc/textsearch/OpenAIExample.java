package com.cpbpc.textsearch;

import com.cpbpc.comms.OpenAIUtil;
import com.cpbpc.comms.SecretUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OpenAIExample {

    private static final String OPENAI_API_KEY = SecretUtil.getOPENAPIKey();
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public static void main(String[] args) throws IOException {
        String question = "I would like to know more things about salvation, faith and theology";

        // Get comprehension from OpenAI
        String jsonResponse = OpenAIUtil.comprehendQuestion(question);
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>(){}.getType();
        Map<String, Object> result = gson.fromJson(jsonResponse, type);
        System.out.println("Comprehension of the question: " + ((Map)((Map)((List)result.get("choices")).get(0)).get("message")).get("content"));

        // Tokenize the question
        List<String> tokens = tokenizeText(question);
        System.out.println("Tokenized question: " + tokens);
    }

    public static List<String> tokenizeText(String text) {
        // Simple tokenization using spaces
        return Arrays.asList(text.split("\\s+"));
    }
}

