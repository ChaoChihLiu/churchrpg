package com.cpbpc;

import java.io.IOException;

public class Encoder {

    public static  void main(String args[]) throws IOException {
//        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("https://www.kepeklian.fr/mm.php")));
//        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("太")));
//
//        System.out.println(URLDecoder.decode("%E8%AF%BB%E7%A5%B7%E9%95%BF"));

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


        for( int i=1; i<=50; i++ ){
            System.out.println("https://cpbpc-bible-reading-plan.s3.ap-southeast-1.amazonaws.com/kjv/Ex"+i+".mp3");
        }

        for( int i=1; i<=50; i++ ){
            System.out.println("太"+i);
        }

    }

}
