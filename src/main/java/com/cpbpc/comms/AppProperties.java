package com.cpbpc.comms;

import org.apache.commons.lang3.StringUtils;

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

    private static Properties urVersionBookMapping = new Properties();
    public static Properties readurVersionBibleMapping(){
        if( !urVersionBookMapping.isEmpty() ){
            return urVersionBookMapping;
        }

        try (InputStream input = AppProperties.class.getClassLoader().getResourceAsStream("urVersion-mapping.properties")) {
            // Load properties from the InputStream
//            bookMapping.load(input);

            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while( (line = br.readLine()) != null ){
                String result = URLDecoder.decode(line).trim();
                urVersionBookMapping.put( result.substring(0, result.indexOf("=")), result.substring(result.indexOf("=")+1) );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return urVersionBookMapping;
    }

    private static Properties edzxBookMapping = new Properties();
    public static Properties readEDZXBibleMapping(){
        if( !edzxBookMapping.isEmpty() ){
            return edzxBookMapping;
        }

        try (InputStream input = AppProperties.class.getClassLoader().getResourceAsStream("edzx-mapping.properties")) {
            // Load properties from the InputStream
//            bookMapping.load(input);

            BufferedReader br = new BufferedReader(new InputStreamReader(input));
            String line = null;
            while( (line = br.readLine()) != null ){
                String result = URLDecoder.decode(line).trim();
                edzxBookMapping.put( result.substring(0, result.indexOf("=")), result.substring(result.indexOf("=")+1) );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return edzxBookMapping;
    }

    public static Properties getConfig() {
        return appProperties;
    }

    public static boolean isAWS() {
        if( AppProperties.getConfig().containsKey("cloud_sys")
                && !StringUtils.isEmpty(AppProperties.getConfig().getProperty("cloud_sys"))
                &&  StringUtils.equals("aws", AppProperties.getConfig().getProperty("cloud_sys")) ){
            return true;
        }
        return false;
    }
    public static boolean isAzure() {
        if( AppProperties.getConfig().containsKey("cloud_sys")
                && !StringUtils.isEmpty(AppProperties.getConfig().getProperty("cloud_sys"))
                &&  StringUtils.equals("azure", AppProperties.getConfig().getProperty("cloud_sys")) ){
            return true;
        }
        return false;
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
