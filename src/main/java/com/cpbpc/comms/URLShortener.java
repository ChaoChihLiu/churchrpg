package com.cpbpc.comms;

import com.amazonaws.internal.ExceptionUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLShortener {
    private static Logger logger = Logger.getLogger(URLShortener.class.getName());

    public static String shorten(String link) throws UnsupportedEncodingException {
        String accessKey = SecretUtil.getBitlyKey();
        String requestBody = "{\"long_url\":\"" + link + "\", \"domain\":\"bit.ly\", \"group_guid\":\"Bn7fexZnrBp\"}";
        StringEntity entity = new StringEntity(requestBody);
        HttpClient httpClient = HttpClientBuilder.create().build();

        // Create an HttpGet request with the API endpoint URL
        HttpPost request = new HttpPost("https://api-ssl.bitly.com/v4/shorten");
        request.setEntity(entity);

        // Set the Authorization header with the access key
        request.setHeader("Authorization", "Bearer " + accessKey);
        request.setHeader("Content-Type", "application/json");

        try {
            // Execute the request and get the response
            HttpResponse response = httpClient.execute(request);
            String responseBody = EntityUtils.toString(response.getEntity());
            return extractLink(responseBody);

            // Process the response as needed
            // Here, you can extract the shortened URL from the response and use it
        } catch (Exception e) {
            logger.info(ExceptionUtils.exceptionStackTrace(e));
        }
        return "";
    }

    private static final String pattern = "(https://bit\\.ly/[0-9A-za-z]+)";
    private static String extractLink(String responseBody) {

        Pattern p = Pattern.compile(pattern);
        String result = "";
        Matcher matcher = p.matcher(responseBody);
        if (matcher.find()) {
            result = matcher.group(0);
        }

        return result;
    }

}
