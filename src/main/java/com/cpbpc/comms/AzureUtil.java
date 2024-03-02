package com.cpbpc.comms;

import org.apache.commons.lang3.StringUtils;

public class AzureUtil {

    public static String toTTS(String content, String voiceId) {

        String[] splits = StringUtils.split(voiceId, "-");
        String language = splits[0] + "-" + splits[1];

        return "<speak\n" +
                "                        xmlns=\"http://www.w3.org/2001/10/synthesis\"\n" +
                "                        xmlns:mstts=\"http://www.w3.org/2001/mstts\"\n" +
                "                        xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\""+language+"\">\n" +
                "                    <voice name=\""+voiceId+"\">\n" +
                "                        <s />\n" +
                generateStyleStart() +
                "                            <prosody rate=\""+calcSpeed()+"%\">\n" +
                content +
                "                            </prosody>\n" +
                generateStyleEnd() +
                "                        <s />\n" +
                "                    </voice>\n" +
                "                </speak>";

    }
    public static String toTTS(String content) {

        return toTTS(content, AppProperties.getConfig().getProperty("voice_id"));

    }

    private static String generateStyleEnd() {

        if( StringUtils.isEmpty(AppProperties.getConfig().getProperty("style"))
                || StringUtils.equals(AppProperties.getConfig().getProperty("style"), "default") ){
            return "";
        }

        return "                        </mstts:express-as>\n";
    }

    private static String generateStyleStart() {

        if( StringUtils.isEmpty(AppProperties.getConfig().getProperty("style"))
         || StringUtils.equals(AppProperties.getConfig().getProperty("style"), "default") ){
            return "";
        }

        return "                        <mstts:express-as style=\""+AppProperties.getConfig().getProperty("style")+"\">\n";

    }

    private static int calcSpeed() {
        int rate = Integer.parseInt(AppProperties.getConfig().getProperty("speech_speed").replace("%", ""));
        return rate - 100;
    }
}
