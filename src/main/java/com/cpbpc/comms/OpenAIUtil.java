package com.cpbpc.comms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAIUtil {

    private static String createThread() throws IOException {
        String apiKey = SecretUtil.getOPENAPIKey();
        String apiUrl = "https://api.openai.com/v1/threads";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("OpenAI-Beta", "assistants=v1");
        connection.setDoOutput(true);
        InputStream is = connection.getInputStream();
        String response = IOUtils.toString(is);

        return extractThreadId(response);
        
    }

    public static String comprehendQuestion(String question) throws IOException {
        String statement = "Comprehend the meaning of the following question and provide keywords or key phrases only, " +
                "including synonyms of keywords/key phrases: '"+question + "\n"
                + "and please organise your answer in array only"
                ;

        String response = createCompletions(statement);
        return response;
    }

    private static Pattern open_ai_thread_response_pattern = Pattern.compile("\"id\":\\s*\"([^\"]+)\"");
    private static String extractThreadId(String resonse) {

        Matcher matcher = open_ai_thread_response_pattern.matcher(resonse);
        while (matcher.find()) {
            String result = matcher.group(1);
            return result;
        }
        return "";
    }

    public static List<String> suggestedIntonation(String text) throws IOException {

        List<String> result = new ArrayList<>();

        String question = "could you give me intonation for this pinyin: "+text+
                ", indicate tone markings with number, and follow this pattern '[pinyin]:[intonation] to organise your response'?";

         createCompletions(question);
         return result;
    }

    public static List<String> suggestedPinyin(String text) throws IOException {
        String question = "could you give me pinyin for '"+text+
                "' in form of x-amazon-pinyin, and follow this pattern '[original word]:[pinyin], [pinyin in form of x-amazon-pinyin]' to organise your response?";

        String response = createCompletions(question);
        System.out.println(response);
        List<String> pinyin = extractPinyin(response);
        return pinyin;
    }

    public static List<String> suggestedIPA(String text) throws IOException {
        String question = "could you give me ipa for '"+text+
                "', and follow this pattern '[original word]:[ipa]' to organise your response?";

        String response = createCompletions(question);
        System.out.println(response);
        List<String> pinyin = extractPinyin(response);
        return pinyin;
    }

    public static String summarise(String text) throws IOException {
        String question = "This is the content of Q n A, could you summarise the answer? Do keep Bible verses in summary if there has any"+text;
//        String question = "could you tell me when is the Chinese New Year in 2024?";

        String response = createCompletions(question);
        System.out.println(response);
        return response;
    }

    private static String createCompletions(String prompt) throws IOException {
        String apiKey = SecretUtil.getOPENAPIKey();
//        String apiUrl = "https://api.openai.com/v1/completions";
        String apiUrl = "https://api.openai.com/v1/chat/completions";
//        String apiUrl = "https://api.openai.com/v1/threads/"+createThread()+"/messages";

        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
//        connection.setRequestProperty("OpenAI-Beta", "assistants=v1");
        connection.setDoOutput(true);

        String requestBody = genCompletionJsonInput(prompt);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        InputStream is = connection.getInputStream();
        String response = IOUtils.toString(is);
        return response;
    }

    private static String genCompletionJsonInput(String text) {

//        String requestData = "{\"messages\": [{\"role\": \"system\", \"content\": \"You are a helpful assistant.\"}, {\"role\": \"user\", \"content\": \"" + prompt + "\"}]}";

        Map<String, Object> elements = new LinkedHashMap<>();
//        elements.put("model", "gpt-3.5-turbo");
//        elements.put("model", "gpt-3.5-turbo-instruct");
        elements.put("model", "gpt-4o");
//        elements.put("model", "text-davinci-003");
//        elements.put("prompt", text);
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, String> input = new HashMap<>();
        input.put("role", "user");
        input.put("content", text);
        list.add(input);

        elements.put("messages", list);
        elements.put("max_tokens", 150);
//        elements.put("temperature", 0.7);
//        elements.put("n", "1");
//        elements.put("max_tokens", "16");


        Gson gson = new Gson();
        Type gsonType = new TypeToken<HashMap>(){}.getType();
        return gson.toJson(elements,gsonType);
    }

    private static Pattern open_ai_response_pattern = Pattern.compile("\"text\":\\s*\"([^\"]+)\"");
    private static String extractCompletionsResponse (String response){
        Matcher matcher = open_ai_response_pattern.matcher(response);
        while (matcher.find()) {
            return StringUtils.remove(matcher.group(1), "\\n");
        }
        return "";
    }
    private static Pattern pinyin_pattern = Pattern.compile("[A-Za-z]+\\d{1,1}");
    private static List<String> extractPinyin(String response) {
        List<String> result = new ArrayList<>();
        String text = extractCompletionsResponse(response);
        Matcher matcher = pinyin_pattern.matcher(text);
        while (matcher.find()) {
            String word = matcher.group(0);
            result.add(word);
        }
        return result;
    }

    private static String genCreateMsgJsonInput(String text) {

        SortedMap<String, Object> elements = new TreeMap();
        elements.put("role", "user");
        elements.put("content", text);


        Gson gson = new Gson();
        Type gsonType = new TypeToken<HashMap>(){}.getType();
        return gson.toJson(elements,gsonType);
    }
    
    private static String genChatCompletionJsonInput(String text) {

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        messages.add(message);
        message.put("role", "assistant");
        message.put("content", text);

        SortedMap<String, Object> elements = new TreeMap();
        elements.put("messages", messages);
//        elements.put("model", "gpt-3.5-turbo");
        elements.put("engine", "text-davinci-003");


        Gson gson = new Gson();
        Type gsonType = new TypeToken<HashMap>(){}.getType();
        return gson.toJson(elements,gsonType);
    }

    public static String speechToText(String filePath, String language) {
        try {
            // Replace 'YOUR_API_KEY' with your OpenAI API key
            String apiKey = SecretUtil.getOPENAPIKey();
            String apiUrl = "https://api.openai.com/v1/audio/transcriptions";


//            URL url = new URL(apiUrl);
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "multipart/form-data");
//            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
//            connection.setDoOutput(true);
//
//            String boundary = "---------------------------" + System.currentTimeMillis();
//            // Set request headers
//            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//
//            // Get output stream to write the file data
//            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
//            addSTTFormField(outputStream, "model", "whisper-1", boundary);
//            addSTTFormField(outputStream, "language", language, boundary);
//            addSTTFormField(outputStream, "response_format", "json", boundary);
//            addSTTFormField(outputStream, "temperature", "0", boundary);
//
//            // Add file parameter to the request
//            addFilePart(outputStream, "file", new File(filePath), boundary);
//
//            // End the request
//            outputStream.writeBytes("--" + boundary + "--\r\n");
//            outputStream.flush();
//            outputStream.close();
//
//            // Get the server's response
//            int responseCode = connection.getResponseCode();
//            System.out.println("Server response code: " + responseCode);
//
//            return IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);

            final HttpPost httppost = new HttpPost(apiUrl);
            httppost.addHeader("Authorization", "Bearer "+apiKey);
            httppost.addHeader("Accept", "application/json");
//            httppost.addHeader("Content-Type", "multipart/form-data");

//            List<NameValuePair> form = new ArrayList<>();
//            form.add(new BasicNameValuePair("model", "whisper-1"));
//            form.add(new BasicNameValuePair("language", language));
//            form.add(new BasicNameValuePair("response_format", "json"));
//            form.add(new BasicNameValuePair("temperature", "0"));
//            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

            final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setContentType(ContentType.MULTIPART_FORM_DATA);
            final File file = new File(filePath);
            builder.addTextBody("model", "whisper-1");
            builder.addTextBody("language", language);
            builder.addTextBody("response_format", "json");
            builder.addTextBody("temperature", "0");
            builder.addPart("file", new FileBody(file));
            final HttpEntity entity = builder.build();
            httppost.setEntity(entity);
            HttpClient client = HttpClientBuilder.create().build();
            final HttpResponse response = client.execute(httppost);
            return IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static void addFilePart(DataOutputStream outputStream,
                                    String fieldName,
                                    File uploadFile,
                                    String boundary) throws IOException {
        // Start the file part
        outputStream.writeBytes("--" + boundary + "\r\n");
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                fieldName + "\"; file=\"");
        outputStream.writeBytes("\r\n");

        // Write the file data
        FileInputStream fileInputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        outputStream.writeBytes("\r\n");
        fileInputStream.close();
    }

    public static void textToSpeech(String text, String voice) throws ProtocolException {
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

            String requestBody = genTTSJsonInput(text, voice);

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

    private static String genTTSJsonInput(String input, String voice) {
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

    private static void addSTTFormField(DataOutputStream outputStream, String fieldName, String value, String boundary) throws IOException {
        // Add a form field
        outputStream.writeBytes("--" + boundary + "\r\n");
        outputStream.writeBytes("Content-Disposition: form-data; name=\"" +
                fieldName + "\"\r\n\r\n");
        outputStream.writeBytes(value + "\r\n");
    }
}

