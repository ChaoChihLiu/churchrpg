package com.cpbpc.comms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
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
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class OpenAIUtil {

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

