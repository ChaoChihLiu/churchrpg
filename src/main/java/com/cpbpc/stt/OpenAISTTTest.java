package com.cpbpc.stt;

import com.cpbpc.comms.OpenAIUtil;

public class OpenAISTTTest {

    public static void main(String[] args){
        System.out.println(OpenAIUtil.speechToText("src/main/resources/crpg20231210.mp3", "zh"));
    }

}
