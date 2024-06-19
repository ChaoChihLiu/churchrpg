package com.cpbpc.stt;

import com.cpbpc.comms.SecretUtil;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class OpenAIChatCompletion {

    public static void main(String[] args) throws IOException {
//        String text = "January 1. Morning. Look unto me. The Bible passage is from Isaiah chapter 45 verse 22. This devotion is entitled. Every man looks to the new year with hope. Another new year dawns on planet earth, and we are still exposed to sorrow, Satan, and disappointment. Sin still lives in us, and a thousand things are ready to distress us. But our God says. Look unto me. Look unto me as the source of happiness, the giver of grace, and your friend. Look unto me in every trial, for all you want, and in every place. Look unto me today. I have blessings to bestow. I am waiting to be gracious. I am your Father in Jesus. Believe that I am deeply interested in your present and eternal welfare. That all I have promised I will perform. That I am with you to bless you. I cannot be unconcerned about anything that affects you, and I pledge to make all things work together for your good. You have looked to self, to others, in times past. But you have only met with trouble and disappointment. Now look unto me alone, to me for all. Lift up the iron heart to me today, and every day through the year, and walk before me in peace and holiness. Prove me now herewith if I will not make you holy, useful, and happy. Try me, and find my word of promise true, true to the very letter. Only look unto me. Look to Him, till His wondrous love thy every thought control. It's vast. Constraining power prove. All body. Spirit. Soul.";

        try {

            String text = IOUtils.toString(new FileInputStream("/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/1.txt"));
            // Replace 'YOUR_API_KEY' with your actual OpenAI API key
            String apiKey = SecretUtil.getOPENAPIKey();
            String endpoint = "https://api.openai.com/v1/chat/completions";

            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);

            // Replace 'YOUR_PROMPT' with the prompt you want to send for completion
//            String prompt = "YOUR_PROMPT";
            String prompt = "could you summarise this article for me?"+text;

            // Replace 'YOUR_EXAMPLES' with the examples or additional parameters as needed
            String requestData = "{\"model\": \"gpt-4\", \"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";
//            {"model":"gpt-4","message":[{"role":"user","content":"could you tell me when is the Chinese New Year in 2024?"}]}
//            {"max_tokens":100,"message":[{"role":"user","content":"could you summarise this article for me?January 1. Morning. Look unto me. The Bible passage is from Isaiah chapter 45 verse 22. This devotion is entitled. Every man looks to the new year with hope. Another new year dawns on planet earth, and we are still exposed to sorrow, Satan, and disappointment. Sin still lives in us, and a thousand things are ready to distress us. But our God says. Look unto me. Look unto me as the source of happiness, the giver of grace, and your friend. Look unto me in every trial, for all you want, and in every place. Look unto me today. I have blessings to bestow. I am waiting to be gracious. I am your Father in Jesus. Believe that I am deeply interested in your present and eternal welfare. That all I have promised I will perform. That I am with you to bless you. I cannot be unconcerned about anything that affects you, and I pledge to make all things work together for your good. You have looked to self, to others, in times past. But you have only met with trouble and disappointment. Now look unto me alone, to me for all. Lift up the iron heart to me today, and every day through the year, and walk before me in peace and holiness. Prove me now herewith if I will not make you holy, useful, and happy. Try me, and find my word of promise true, true to the very letter. Only look unto me. Look to Him, till His wondrous love thy every thought control. It\u0027s vast. Constraining power prove. All body. Spirit. Soul."}],"model":"gpt-4"}

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            InputStream is = connection.getInputStream();
            String response = IOUtils.toString(is);
            System.out.println("Response Code: " + responseCode);
            System.out.println(response);

            // Handle the API response here (read from the connection's input stream, etc.)

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
