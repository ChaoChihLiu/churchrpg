package com.cpbpc.comms;

public class AzureUtil {
    public static String toTTS(String content) {

        return "<speak\n" +
                "                        xmlns=\"http://www.w3.org/2001/10/synthesis\"\n" +
                "                        xmlns:mstts=\"http://www.w3.org/2001/mstts\"\n" +
                "                        xmlns:emo=\"http://www.w3.org/2009/10/emotionml\" version=\"1.0\" xml:lang=\"zh-CN\">\n" +
                "                    <voice name=\""+AppProperties.getConfig().getProperty("voice_id")+"\">\n" +
                "                        <s />\n" +
                "                        <mstts:express-as style=\""+AppProperties.getConfig().getProperty("style")+"\">\n" +
                "                            <prosody rate=\""+calcSpeed()+"%\">\n" +
                                                content +
                "                            </prosody>\n" +
                "                        </mstts:express-as>\n" +
                "                        <s />\n" +
                "                    </voice>\n" +
                "                </speak>";

    }

    private static int calcSpeed() {
        int rate = Integer.parseInt(AppProperties.getConfig().getProperty("speech_speed").replace("%", ""));
        return rate - 100;
    }
}
