package com.cpbpc.rpgv3.zh;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ClaudePdfExtractor {

    public static void main(String[] args) throws Exception {
        try{
            // Step 1: Load PDF file and prompt
            File pdfFile = new File("/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/sample.pdf");
            byte[] pdfData = Files.readAllBytes(pdfFile.toPath());
            String prompt = "Extract all Bible references and titles from the first page of the attached PDF.";

            // Step 2: Build multipart/form-data request body
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart("file", new ByteArrayBody(pdfData, ContentType.APPLICATION_PDF, "document.pdf"))
                    .addPart("prompt", new StringBody(prompt, ContentType.TEXT_PLAIN))
                    .build();

            // Step 3: Prepare the HTTP POST request
            HttpPost post = new HttpPost("https://bedrock-runtime.ap-southeast-1.amazonaws.com/api/claude3.5/ocr");
            post.setEntity(entity);
//        post.setHeader("Authorization", "Bearer YOUR_API_KEY"); // Or use AWS SigV4 signer if going to Bedrock
            post.setHeader("Accept", "application/json");

            // Step 4: Send the request and print the response
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpClientResponseHandler<String> responseHandler = httpResponse ->
                        EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

                String response = client.execute(post, responseHandler);
                System.out.println("Claude OCR response:\n" + response);
            }
        }catch (Exception e){
            e.printStackTrace();    
        }
    }
}

