package com.cpbpc.comms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class OpenAIUtil {
    public static void toOpenAI(String text, String voice) throws ProtocolException {
        try {
            // Replace 'YOUR_API_KEY' with your OpenAI API key
            String apiKey = SecretUtil.getOPENAPIKey();
            String apiUrl = "https://api.openai.com/v1/audio/speech";


            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);

            String requestBody = genJsonInput(text, voice);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            InputStream is = connection.getInputStream();
            OutputStream outstream = new FileOutputStream(new File("file.mp3"));
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) > 0) {
                outstream.write(buffer, 0, len);
            }
            outstream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String genJsonInput(String input, String voice) {
        SortedMap<String, String> elements = new TreeMap();
//        elements.put("input", URLEncoder.encode(input));
        elements.put("input", input);
        elements.put("model", "tts-1");
//        elements.put("language", "english");
        elements.put("voice", voice);
        elements.put("response_format", "mp3");
        elements.put("speed", "0.8");

        Gson gson = new Gson();
        Type gsonType = new TypeToken<HashMap>(){}.getType();
        return gson.toJson(elements,gsonType);
    }
}

