package com.cpbpc;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Encoder {

    public static  void main(String args[]) throws IOException {

//        String content = "134-1234 大卫王年纪老迈，虽用被遮盖，仍不觉暖。";
////        System.out.println(content.replaceAll("\\.\\.\\.", ".").replaceAll("\\.\\.", "."));
//        System.out.println(content.replaceAll("^\\d+[-\\d]{0,}", ""));

//        String replaced = "Balaam also the son of ";
//         replaced = replaced.replace("^" +"Balaam"+" ", " " + "test" + " ")  ;
//         System.out.println(replaced);

//        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("https://www.kepeklian.fr/mm.php")));
        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("启")));

        
//        System.out.println(Integer.parseInt(StringUtils.substring("rpg/2024_06/arpg20240630-1.mp3", StringUtils.indexOf("rpg/2024_06/arpg20240630-1.mp3", "-")+1, StringUtils.indexOf("rpg/2024_06/arpg20240630-1.mp3", ".mp3"))));
//
        System.out.println(URLDecoder.decode("%E8%AF%97"));

//        String htmlString = "<p>This is a <b>bold</b> &amp; <i>italic</i> text.</p>";
//        String escapedHtml = StringEscapeUtils.unescapeHtml4(htmlString);
//        System.out.println("Escaped HTML: " + escapedHtml);


//        Properties properties = new Properties();
//        properties.load(new FileReader(new File("/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/bible-mapping.properties")));
//
//        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
//        for( Map.Entry<Object, Object> entry : entries ){
//            System.out.println( URLDecoder.decode((String)entry.getKey(), StandardCharsets.UTF_8) + "=" + (String)entry.getValue());
//        }
//        ssh -L 3306:localhost:3306 tc.george@dst.com.bn@sshbastion1.ams-dst.com
//        ssh -L 3306:stack-8zctttqoyu8qkvjgp-rdsinstance-w4dyk0s0uosq.ci4ubilvqzsx.ap-southeast-1.rds.amazonaws.com:3306 tc.george@dst.com.bn@sshbastion1.ams-dst.com -N

        System.out.println(StringUtils.rightPad("", 10, "-"));
        
        for( int i = 1; i<= 31; i++ ){
            String num = String.valueOf(i);
            if( i<10 ){
                num = "0"+String.valueOf(i);
            }
//            System.out.println("https://cpbpc-tts.s3.ap-southeast-1.amazonaws.com/remembrance/May/dr_May_"+num+"_Morning.mp3");
//            System.out.println("https://cpbpc-tts.s3.ap-southeast-1.amazonaws.com/remembrance/May/dr_May_"+num+"_Evening.mp3");
//            System.out.println("https://cpbpc-bible-reading-plan.s3.ap-southeast-1.amazonaws.com/kjv/Prov"+i+".mp3");
//            System.out.println("https://cpbpc-bible-reading-plan.s3.ap-southeast-1.amazonaws.com/cuvs/%E5%90%AF"+i+".mp3");
            System.out.println("https://cpbpc-rpg-audio.s3.ap-southeast-1.amazonaws.com/rpg/2025_08/arpg202508"+num+".mp3");
//            System.out.println("https://cpbpc-rpg-audio.s3.ap-southeast-1.amazonaws.com/rpg-chinese/2025_07/crpg202507"+num+".mp3");
        }

//        for( int i=1; i<=50; i++ ){
//            System.out.println("https://cpbpc-bible-reading-plan.s3.ap-southeast-1.amazonaws.com/kjv/Ex"+i+".mp3");
//        }
//
//        for( int i=1; i<=50; i++ ){
//            System.out.println("太"+i);
//        }

//        String prefix_regex = "/[A-Za-z|\\u4E00-\\u9FFF]+/\\d{1,3}/";
//        Pattern p = Pattern.compile(prefix_regex);
//        List<String> inputs = Arrays.asList( "cuvs/創世記1/14/16.xml", "cuvs/出埃及記/14/16.xml", "kjv/Exodus/1140/26.xml", "kjv/測試Matthew/12/26.xml" );
//        for( String input: inputs ){
//            Matcher matcher = p.matcher(input);
//            System.out.println(matcher.find());
//        }

//        List<String> input = Arrays.asList("Exodus|1");
//        System.out.println(StringUtils.join(input, ","));

        String text = "This is a <test> string with some words that should not match. Another example <tag>Word</tag> is given.";
        Pattern pattern = Pattern.compile("(?<![<>])(Word)+(?![<>])");
        Matcher m = pattern.matcher(text);

        while (m.find()) {
            System.out.println(m.group());
        }

        // Specify the access point ARN
//        String accessPointArn = "arn:aws:s3:ap-southeast-1:216503848453:accesspoint/cpbpc-rpg-audio-access-point";
//
//        // Create an S3 client with the access point configuration
//        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//                .withCredentials(new DefaultAWSCredentialsProviderChain())
//                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
//                        "https://" + accessPointArn + ".s3-ap-southeast-1.amazonaws.com",
//                        "ap-southeast-1")) // Replace "us-west-2" with your region
//                .build();
//
//        // List objects in the bucket through the access point
//        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request().withBucketName(accessPointArn);
//        ListObjectsV2Result objectListing = s3Client.listObjectsV2(listObjectsRequest);
//
//        // Print object keys
//        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
//            System.out.println("Object Key: " + objectSummary.getKey());
//        }

    }

}
