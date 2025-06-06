package com.cpbpc.comms;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import net.sourceforge.pinyin4j.PinyinHelper;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.NumberConverter.chineseToNumber;

public class TextUtil {

    public static String escapeSpecialChar(String title) {
        if( StringUtils.isEmpty(title) ){
            return "";
        }

        return title.replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)")
                .replaceAll("\\?", "\\\\?")
                .replaceAll("\\!", "\\\\!")
//                .replaceAll("（", "\\（")
//                .replaceAll("）", "\\）")
                ;

    }

    private static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
    public static String currentDateTime(){
        Date today = new Date();
        return dateTimeFormat.format(today);
    }

    public static String capitalizeEveryWord(String sentence) {
        String[] words = sentence.split(" ");
        StringBuilder capitalizedSentence = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                String firstLetter = word.substring(0, 1);
                String restOfWord = word.substring(1);
                String capitalizedWord = firstLetter.toUpperCase() + restOfWord.toLowerCase();
                capitalizedSentence.append(capitalizedWord).append(" ");
            }
        }

        return capitalizedSentence.toString().trim();
    }

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static String currentDate(){
        Date today = new Date();
        return dateFormat.format(today);
    }

    public static String encodeChineseWord(String input) {

        String result = "";
        for( char c : input.toCharArray() ){

            if(ZhConverterUtil.isChinese(c)){
                result += URLEncoder.encode(String.valueOf(c));
            }else{
                result += String.valueOf(c);
            }

        }

        return result;
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

    public static String removeDoubleQuote( String input ){
        String result = input;

        List<String> list = List.of("\"", "“");
        for( String item : list ){
            result = result.replaceAll(item, "");
            result = result.replaceAll(StringEscapeUtils.escapeHtml4(item), "");
        }

        return result;
    }

    public static String removeXMLTag(String input){
        return removeXMLTag(input,"");
    }

    public static String removeXMLTag(String input, String replacement){
        if (StringUtils.isEmpty(input)) {
            return "";
        }

        return input.replaceAll("<[^>]+>", replacement);
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

    public static String replaceHtmlSpace(String input) {
        return RegExUtils.replaceAll(input, "&nbsp;", " ");
    }

    public static String removeZhWhitespace( String content ){
        String result = content.replaceAll("(\\p{IsHan})\\s+(?=\\p{IsHan})", "$1");
        return result;
    }

    public static Pattern getDatePattern() {
        return Pattern.compile("\\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{1,2})\\s+\\b");
    }

    public static String insertWhitespace(String input) {
        if( StringUtils.isEmpty(input) ){
            return input;
        }

        char[] chars = input.toCharArray();
        StringBuffer buffer = new StringBuffer();
        for( char c : chars ){
            buffer.append(c).append(" ");
        }

        return buffer.toString();
    }

    public static String convertChineseDate(String chineseDate) {
        int month = chineseToNumber(chineseDate.substring(0, chineseDate.indexOf("月")));
        int day = chineseToNumber(
                chineseDate.substring(chineseDate.indexOf("月") + 1, chineseDate.indexOf("日"))
        );
        return String.format("%02d-%02d", month, day);
    }

    private static final Pattern chinese_date_pattern = Pattern.compile("((?:一|二|三|四|五|六|七|八|九|十|十一|十二)月(?:[一二三四五六七八九十]{1,3})日)[，、]?(?:[^一二三四五六七八九十早晚]*)(早晨|傍晚)?");
    public static String getChieseDate(String summary) {

        Matcher matcher = chinese_date_pattern.matcher(summary);
        while( matcher.find() ){
            String matched = matcher.group(1);
            if( StringUtils.isEmpty(matched) ){
                return "";
            }

            return matched.trim();
        }

        return "";
    }

    public static String getChieseTiming(String summary) {
        Matcher matcher = chinese_date_pattern.matcher(summary);
        while( matcher.find() ){
            String timing = matcher.group(2);
            if( StringUtils.isEmpty(timing) ){
                return "";
            }
            return timing.trim();
        }

        return "";
    }

    public static String chieseTimingToEnglish(String input) {
        if( StringUtils.equals(input, "早晨") ){
            return "morning";
        }

        return "evening";
    }

}
