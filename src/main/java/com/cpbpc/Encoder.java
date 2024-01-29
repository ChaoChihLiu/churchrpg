package com.cpbpc;

import com.github.houbb.opencc4j.util.ZhConverterUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Encoder {

    public static  void main(String args[]) throws IOException {
        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("歌林多前書")));
        System.out.println(URLEncoder.encode(ZhConverterUtil.toSimple("歌林多後書")));

        System.out.println(URLDecoder.decode("%E8%AF%BB%E7%A5%B7%E9%95%BF"));


        Properties properties = new Properties();
        properties.load(new FileReader(new File("/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/bible-mapping.properties")));

        Set<Map.Entry<Object, Object>> entries = properties.entrySet();
        for( Map.Entry<Object, Object> entry : entries ){
            System.out.println( URLDecoder.decode((String)entry.getKey(), StandardCharsets.UTF_8) + "=" + (String)entry.getValue());
        }
    }

}
