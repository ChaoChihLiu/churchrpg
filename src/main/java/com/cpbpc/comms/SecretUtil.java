package com.cpbpc.comms;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class SecretUtil {

    private static Properties secrets = new Properties();
    static{
        try {
            secrets.load(new FileReader(new File(".secret")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private SecretUtil(){
    }

    public static String getOPENAPIKey(){
        return secrets.getProperty("openai_api_key");
    }

    public static String getBitlyKey() {
        return secrets.getProperty("bitly_api_key");
    }
    public static String getAzureSpeechKey() {
        return secrets.getProperty("azure_speech_api_key");
    }
    public static String getTelegramBotKey(){
        return secrets.getProperty("telemgram_bot_api_key");
    }
}
