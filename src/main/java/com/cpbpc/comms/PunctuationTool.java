package com.cpbpc.comms;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PunctuationTool {

    private static final String[] hyphens_unicode = new String[]{"\\u002d", "\\u2010", "\\u2011", "\\u2012", "\\u2013", "\\u2014", "\\u2015", "\\u2212", "\\u2500"};

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
    public static String[] getHyphens(){
        List<String> punctuations = new ArrayList<>();
        for (String hyphen_unicode : hyphens_unicode) {
            punctuations.add(StringEscapeUtils.unescapeJava(hyphen_unicode));
        }
        return punctuations.toArray(new String[]{});
    }

    public static String changeFullCharacter(String input){
        return input.replaceAll("：", ":")
                .replaceAll("，", ",")
                .replaceAll("、", ",")
                .replaceAll("；", ";")
                .replaceAll("？", "?")
                .replaceAll("！", "!")
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
//        String result = input
//                            .replaceAll("\\.", getPauseTag(800))
//                            .replaceAll(",", getPauseTag(200))
//                            .replaceAll(":", getPauseTag(800))
//                            .replaceAll(";", getPauseTag(400))
//                            .replaceAll("\\?", getPauseTag(800))
//                            .replaceAll("\\!", getPauseTag(800))
//                            .replaceAll("\\(", getPauseTag(200))
//                            .replaceAll("\\)", getPauseTag(200))
////                            .replaceAll("\"", "")
//                            .replaceAll("”", "")
//                            .replaceAll("“", "")
//                ;
//
//        if( containHyphen(input) ){
//            String hyphen = getHyphen(input);
//            result = result.replaceAll(hyphen, getPauseTag(200));
//        }
//
//        return result;
        return input;
    }

    public static String replacePunctuationWithBreakTag(String input){
        String result = input
                .replaceAll("\\.", pause(800))
                .replaceAll(",", pause(200))
                .replaceAll(":", pause(200))
                .replaceAll(";", pause(400))
                .replaceAll("\\?", pause(800))
                .replaceAll("\\!", pause(800))
                .replaceAll("\\(", pause(200))
                .replaceAll("\\)", pause(200))
//                .replaceAll("\"", "")
                .replaceAll("”", "")
                .replaceAll("“", "")
//                .replaceAll("…", pause(200))
                ;

        if( containHyphen(result) ){
            Pattern pattern = Pattern.compile("[\\u4E00-\\u9FFF](["+StringUtils.join(getHyphensUnicode())+"\\s]+)[\\u4E00-\\u9FFF]");
            Matcher matcher = pattern.matcher(result);
            int start = 0;
            String result1 = result;
            while( matcher.find(start) ){
                String target = matcher.group(0);
                String hyphen = getHyphen(target);
                start = matcher.end();
                String replacement = StringUtils.replace(target, hyphen, pause(100));
                result1 = StringUtils.replace(result1, target, replacement);
            }
            result = result1;
        }

        return result;
    }

    public static String removeDoubleQuote(String input){
        String result = input
                .replaceAll("\"", "")
                .replaceAll("”", "")
                .replaceAll("“", "")
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

    private static final Pattern pattern = Pattern.compile("\\[pause(\\d{0,})\\]");
    public static String replacePauseTag( String input ){
        Matcher matcher = pattern.matcher(input);
        String result = input;
        while( matcher.find() ){
            String timespan = matcher.group(1);
            result = result.replace(matcher.group(0), pause(timespan));
        }

        return result;
    }
    public static String replacePauseTag( String input, String replacement ){
        Matcher matcher = pattern.matcher(input);
        String result = input;
        while( matcher.find() ){
            result = result.replace(matcher.group(0), replacement);
        }

        return result;
    }


    private static Pattern chinese_punctuation_pattern = Pattern.compile("[\\u3000-\\u303F\\uFF01-\\uFF5E]");
    public static String removeChinesePunctuation(String text) {
        Matcher matcher = chinese_punctuation_pattern.matcher(text);
        String result = matcher.replaceAll(" ");
        return result;
    }
    private static Pattern punctuation_pattern = Pattern.compile("[\\p{P}]");
    public static String removePunctuation(String text) {
        return removePunctuation(text, " ");
    }
    public static String removePunctuation(String text, String replacement) {
        Matcher matcher = punctuation_pattern.matcher(text);
        String result = matcher.replaceAll(replacement);
        return result;
    }

    public static boolean containQuestionMark(String key) {
        if( StringUtils.contains(key, "?") || StringUtils.contains(key, "？") ){
            return true;
        }
        return false;
    }

    public static String escapeQuestionMark(String key) {
        if( StringUtils.contains(key, "?") || StringUtils.contains(key, "？") ){
            return key.replaceFirst("\\?", "\\\\?").replaceFirst("？", "\\？");
        }

        return key;
    }
}
