package com.cpbpc.rpg;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
{"groups":[{"created":"2023-07-15T14:48:05+0000","modified":"2023-07-15T14:48:05+0000","bsds":[],"guid":"Bn7fexZnrBp","organization_guid":"On7feMz6efC","name":"o_7pdisfg32p","is_active":true,"role":"org-admin","references":{"organization":"https://api-ssl.bitly.com/v4/organizations/On7feMz6efC"}}]}
 */

public class URLShortener {
    private static final String pattern = "(https://bit\\.ly/[0-9A-za-z]+)";

    public static void main(String[] args) throws UnsupportedEncodingException {

        String year = "2024";
        String month = "01";

        for (int i = 1; i <= 31; i++) {
            String date = String.valueOf(i);
            if (i < 10) {
                date = "0" + i;
            }

            // Replace YOUR_ACCESS_KEY with your actual bit.ly access key
            String accessKey = "";

            // Replace YOUR_LONG_URL with the URL you want to shorten
//                String longUrl = "https://cpbpc-rpg-audio.s3.ap-southeast-1.amazonaws.com/rpg/"+year+"_"+month+"/arpg"+year+month+date+".mp3";

            String longUrl = "https://cpbpc-rpg-audio.s3.ap-southeast-1.amazonaws.com/rpg-chinese/"+year+"_"+month+"/crpg"+year+month+date+ ".mp3";
            System.out.println(longUrl);

            // Construct the API endpoint URL
            //        String apiUrl = "https://api-ssl.bitly.com/v4/shorten?long_url=" + longUrl;
//                String requestBody = "{\"long_url\":\""+longUrl+"\", \"domain\":\"bit.ly\", \"group_guid\":\"Bn7fexZnrBp\"}";
//                StringEntity entity = new StringEntity(requestBody);

            //        {
            //        "long_url": "https://dev.bitly.com",
            //                "domain": "bit.ly",
            //                "group_guid": "Ba1bc23dE4F"
            //    }

            // Create an HttpClient
//                HttpClient httpClient = HttpClientBuilder.create().build();
//
//                // Create an HttpGet request with the API endpoint URL
//                HttpPost request = new HttpPost("https://api-ssl.bitly.com/v4/shorten");
//                request.setEntity(entity);
//
//                // Set the Authorization header with the access key
//                request.setHeader("Authorization", "Bearer " + accessKey);
//                request.setHeader("Content-Type", "application/json");
//
//                try {
//                    // Execute the request and get the response
//                    HttpResponse response = httpClient.execute(request);
//                    String responseBody = EntityUtils.toString(response.getEntity());
//                    System.out.println(longUrl + ", " + extractLink(responseBody));
//
//                    // Process the response as needed
//                    // Here, you can extract the shortened URL from the response and use it
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
        }
    }

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

