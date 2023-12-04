package com.cpbpc.comms;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

public class AppProperties {

    private static final Properties appProperties = new Properties();
    private static final AtomicLong totalLength = new AtomicLong(0);

    private AppProperties() {
    }

    public static boolean isChinese(){
        if( appProperties.getProperty("language").equals("zh") ){
            return true;
        }
        return false;
    }
    public static boolean isEnglish(){
        if( appProperties.getProperty("language").equals("en") ){
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
