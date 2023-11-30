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

}
