package com.cpbpc;

import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Encoder {

    public static  void main(String args[]) throws IOException {
//        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("https://www.kepeklian.fr/mm.php")));
        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("出")));
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

        for( int i = 1; i<= 40; i++ ){
            String num = String.valueOf(i);
//            if( i<10 ){
//                num = "0"+String.valueOf(i);
//            }
//            System.out.println("https://cpbpc-tts.s3.ap-southeast-1.amazonaws.com/remembrance/July/dr_July_"+num+"_Morning.mp3");
//            System.out.println("https://cpbpc-tts.s3.ap-southeast-1.amazonaws.com/remembrance/July/dr_July_"+num+"_Evening.mp3");
//            System.out.println("https://cpbpc-bible-reading-plan.s3.ap-southeast-1.amazonaws.com/kjv/Luk"+i+".mp3");
//            System.out.println("https://cpbpc-bible-reading-plan.s3.ap-southeast-1.amazonaws.com/cuvs/%E5%87%BA"+i+".mp3");
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
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            System.out.println(matcher.group());
        }

    }

}
