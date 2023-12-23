package com.cpbpc.comms;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtil {

    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    public static String currentDateTime(){
        Date today = new Date();
        return dateTimeFormat.format(today);
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static String currentDate(){
        Date today = new Date();
        return dateFormat.format(today);
    }

    public static String getChineseWord(String input) {

        String result = "";
        for( char c : input.toCharArray() ){

            if(ZhConverterUtil.isChinese(c)){
                result += String.valueOf(c);
            }

        }

        return result;
    }
    public static boolean containsUnicodeEscape(String input) {
        return input.matches(".*\\\\u[0-9A-Fa-f]{4}.*");
    }

    public static String returnChapterWord(String book){
        if( AppProperties.isChinese() ){
            if( StringUtils.equals(ZhConverterUtil.toSimple(book), ZhConverterUtil.toSimple("詩篇"))
                    || StringUtils.equals(ZhConverterUtil.toSimple(book), ZhConverterUtil.toSimple("詩"))){
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

    public static String convertToPinyin(String chineseText) {
        StringBuilder pinyinBuilder = new StringBuilder();

        for (char c : chineseText.toCharArray()) {
            if (Character.toString(c).matches("[\\u4E00-\\u9FA5]+")) {
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(c);
                if (pinyinArray != null && pinyinArray.length > 0) {
                    pinyinBuilder.append(pinyinArray[0]); // Take the first Pinyin if there are multiple
                } else {
                    pinyinBuilder.append(c); // If Pinyin conversion is not available, keep the original character
                }
            } else {
                pinyinBuilder.append(c); // Keep non-Chinese characters as is
            }
        }

        return pinyinBuilder.toString();
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

    public static String findChapterWord(String verse) {

        if( AppProperties.isChinese() ){
            if( StringUtils.contains(ZhConverterUtil.toSimple(verse), ZhConverterUtil.toSimple("篇"))){
                return ZhConverterUtil.toSimple("篇");
            }
            return "章";
        }
        
        return "";

    }
}
