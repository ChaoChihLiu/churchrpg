package com.cpbpc.stt;

import com.cpbpc.comms.OpenAIUtil;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class OpenAISTTTest {

    public static void main(String[] args){

        Gson gson = new Gson();
        Map result = gson.fromJson(OpenAIUtil.speechToText("src/main/resources/crpg20231210.mp3", "zh"), HashMap.class);

        System.out.println(ZhConverterUtil.toSimple(String.valueOf(result.get("text"))));
    }

}
