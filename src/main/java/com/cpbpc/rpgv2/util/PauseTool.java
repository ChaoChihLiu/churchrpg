package com.cpbpc.rpgv2.util;

import org.apache.commons.lang3.StringUtils;

public class PauseTool {

    public static String changeFullCharacter(String input){
        return input.replaceAll("：", ":")
                .replaceAll("，", ",")
                .replaceAll("、", ",")
                .replaceAll("；", ";")
                .replaceAll("？", "?")
                .replaceAll("。", ".")
                .replaceAll("（", "(")
                .replaceAll("）", ")")
                ;
    }

    public static String getPauseTag(int pause) {
//        return "<break time=\""+pause+"ms\"/>";
        return "[pause" + pause + "]";
    }

    public static String replacePunctuationWithPause(String input){
        String result = input
                            .replaceAll("\\.", getPauseTag(400))
                            .replaceAll(",", getPauseTag(200))
                            .replaceAll(":", getPauseTag(200))
                            .replaceAll(";", getPauseTag(200))
                            .replaceAll("\\?", getPauseTag(200))
                            .replaceAll("\\(", getPauseTag(200))
                            .replaceAll("\\)", getPauseTag(200))
                ;
        return result;
    }

    public static String pause(int timespan) {
        if( timespan == 0 ){
            return "<break time='200ms'/>";
        }
        return "<break time='" + timespan + "ms'/>";
    }
    public static String pause (String timespan){
        if(StringUtils.isEmpty(timespan)){
            return pause(0);
        }

        return pause(Integer.valueOf(timespan));
    }

}
