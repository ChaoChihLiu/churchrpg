package com.cpbpc.comms;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class AppProperties {

    private static final Properties appProperties = new Properties();
    private static final AtomicLong totalLength = new AtomicLong(0);

    private AppProperties() {
    }

    public static boolean isChinese(){
        if( "zh".equals(appProperties.getProperty("language")) ){
            return true;
        }
        return false;
    }
    public static boolean isEnglish(){
        if( "en".equals(appProperties.getProperty("language")) ){
            return true;
        }
        return false;
    }

    public static  void loadConfig(String filePath){
        String propPath = filePath;
        FileInputStream in = null;
        try {
            in = new FileInputStream(propPath);
            appProperties.load(in);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void loadConfig(){
        String propPath = System.getProperty("app.properties");
        FileInputStream in = null;
        try {
            in = new FileInputStream(propPath);
            appProperties.load(in);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static Properties bookMapping = new Properties();
    public static Properties readBibleMapping(){
        if( !bookMapping.isEmpty() ){
            return bookMapping;
        }

        try (InputStream input = AppProperties.class.getClassLoader().getResourceAsStream("bible-mapping.properties")) {
            // Load properties from the InputStream
//            bookMapping.load(input);

            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while( (line = br.readLine()) != null ){
                String result = URLDecoder.decode(line).trim();
                bookMapping.put( result.substring(0, result.indexOf("=")), result.substring(result.indexOf("=")+1) );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookMapping;
    }

    public static Properties getConfig() {
        return appProperties;
    }

    public static long getTotalLength() {
        return totalLength.get();
    }

    public static void addTotalLength(long count) {
        totalLength.addAndGet(count);
    }

    public static void resetTotalLength() {
        totalLength.set(0);
    }
}
