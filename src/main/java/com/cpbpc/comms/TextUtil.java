package com.cpbpc.comms;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    public static String returnChapterWord(String book){
        if( AppProperties.isChinese() ){
            if( StringUtils.equals(book, "詩篇")
                    || StringUtils.equals(book, "詩")
                    || StringUtils.equals(book, ZhConverterUtil.toSimple("詩篇"))
                    || StringUtils.equals(book, ZhConverterUtil.toSimple("詩"))){
                return "篇";
            }
            return "章";
        }
        if( AppProperties.isEnglish() ){
            if( StringUtils.equalsIgnoreCase(book, "psalm") || StringUtils.equalsIgnoreCase(book, "psalms") ){
                return "";
            }
            return "chapter";
        }
        return "";
    }

    public static String removeHtmlTag(String input){
        return removeHtmlTag(input," ");
    }

    public static String removeHtmlTag(String input, String replacement){
        if (StringUtils.isEmpty(input)) {
            return "";
        }

        return input.replaceAll("<[^>]*>|&nbsp;|&zwnj;|&raquo;|&laquo;|&gt;", replacement);
//                .replaceAll("&nbsp;", " ")  ;
    }

    private static Pattern line_break_pattern = Pattern.compile("[\\n|\\r\\n]");
    public static String removeLinkBreak(String text) {
        Matcher matcher = line_break_pattern.matcher(text);
        String result = matcher.replaceAll(" ");
        return result;
    }

    private static Pattern multi_space_pattern = Pattern.compile("\\s+");
    public static String removeMultiSpace(String input) {
        Matcher matcher = multi_space_pattern.matcher(input);
        String result = matcher.replaceAll(" ");
        return result;
    }

    public static boolean hasCaron(char c) {
        return String.valueOf(c).matches("\\p{M}");
    }

    public static String convertToUnicode(String input) {
        StringBuilder unicodeString = new StringBuilder();

        for (char c : input.toCharArray()) {
            unicodeString.append("\\u").append(String.format("%04x", (int) c));
        }

        return unicodeString.toString();
    }
}
