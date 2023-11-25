package com.cpbpc.comms;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class PunctuationTool {

    private static final String[] hyphens_unicode = new String[]{"\\u002d", "\\u2010", "\\u2011", "\\u2012", "\\u2013", "\\u2015", "\\u2212"};

    public static List<String> getAllowedPunctuations() {
        List<String> punctuations = new ArrayList<>();
        punctuations.addAll(List.of(":", ",", " ", ";", "：", "，", "；"));
        for (String hyphen_unicode : hyphens_unicode) {
            punctuations.add(StringEscapeUtils.unescapeJava(hyphen_unicode));
        }
        return punctuations;
    }

    public static String[] getHyphensUnicode(){
        return hyphens_unicode;
    }

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

    public static String getHyphen(String verseStr) {

        for (String hyphen_unicode : getHyphensUnicode()) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verseStr.contains(hyphen)) {
                return hyphen;
            }
        }

        return "";
    }

    public static boolean containHyphen(String verseStr) {

        for (String hyphen_unicode : getHyphensUnicode()) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verseStr.contains(hyphen)) {
                return true;
            }
        }

        return false;
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
