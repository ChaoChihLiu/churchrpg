package com.cpbpc.rpgv2.zh;


import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.TextUtil;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.changeFullCharacter;
import static com.cpbpc.comms.TextUtil.removeHtmlTag;


public class BibleVerseGrab {
    private static Logger logger = Logger.getLogger(BibleVerseGrab.class.getName());

    private static final String[] hyphens_unicode = new String[]{"\\u002d", "\\u2010", "\\u2011", "\\u2012", "\\u2013", "\\u2015", "\\u2212"};
    private static Map<String, String> textCacheBGW = new HashMap<>();
    private static Map<String, String> textCacheEDZX = new HashMap<>();
    private static Map<String, String> textCacheUrVersion = new HashMap<>();

    private static boolean containHyphen(String verseStr) {

        for (String hyphen_unicode : hyphens_unicode) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verseStr.contains(hyphen)) {
                return true;
            }
        }

        return false;
    }

    private static String getHyphen(String verseStr) {

        for (String hyphen_unicode : hyphens_unicode) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verseStr.contains(hyphen)) {
                return hyphen;
            }
        }

        return "";
    }

    static int toNumber(String s) {
//        if (NumberUtils.isCreatable(s)) {
//            return NumberUtils.createInteger(s);
//        }
//
//        String x = " 一二三四五六七八九十百";
//        int l = s.length(),
//                i = x.indexOf(s.charAt(l - 1)),
//                j = x.indexOf(s.charAt(0));
//        if (l < 2) return i; // 1-10
//        if (l < 3) {
//            if (i == 10) return j * 10; // 20,30,40,50,60,70,80,90
//            if (i > 10) return j * 100; // 100,200,300,400,500,600,700,800,900
//            return 10 + i; // 11-19
//        }
//        if (l < 4) return j * 10 + i; // 21-29,31-39,41-49,51-59,61-69,71-79,81-89,91-99
//        if (l < 5) return j * 100 + i; // 101-109,201-209,301-309,401-409,501-509,601-609,701-709,801-809,901-909
//        return j * 100 + i + x.indexOf(s.charAt(2)) * 10; // 111-119,121-129,131-139,...,971-979,981-989,991-999

        if (NumberUtils.isCreatable(s)) {
            return NumberUtils.createInteger(s);
        }

        String x = " 一二三四五六七八九十百";
        int l = s.length();
        int result = 0;

        // Handle hundreds place
        int hundreds = s.contains("百") ? x.indexOf(s.charAt(s.indexOf("百") - 1)) : 0;

        // Handle tens place
        int tens = 0;
        if (s.contains("十")) {
            int tenIndex = s.indexOf("十");
            tens = (tenIndex > 0) ? x.indexOf(s.charAt(tenIndex - 1)) : 1; // Use 1 if "十" is the first character
        }

        // Handle ones place
        int ones = (s.charAt(l - 1) != '十' && s.charAt(l - 1) != '百') ? x.indexOf(s.charAt(l - 1)) : 0;

        if (hundreds > 0) {
            result += hundreds * 100;
        }

        if (tens > 0 || s.contains("十")) { // Handle cases like "十" (10), "二十" (20), etc.
            result += tens * 10;
        }

        result += ones;

        return result;

    }

    public static void main(String[] args) throws IOException {
        String language = "chinese";
        String propPath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-"+language+".properties";
        FileInputStream in = new FileInputStream(propPath);
        AppProperties.getConfig().load(in);
        //        String verseStr = "三十二篇7-9节";
//        String verseStr = "31篇7,11节";
//        String verseStr = "三章7-9节";
//        String book = "民数记";
//        String book = "申命記";
//        String book = "羅馬書";
        String book = "列王纪上";
//        String verseStr = "三十二篇1节-三十六篇";
//        String verseStr = "十八章41至十九章7节";
//        String verseStr = "三十四篇7-9节";
//        String verseStr = "三十四篇";
//        String verseStr = "三十四篇-三十六篇";
//        String verseStr = "一百二十七至一百二十八篇";
//        String verseStr = "十二章";
        String verseStr = "1章";
        System.out.println(grab(book, verseStr));
    }

    public static String grab(String book, String verseStr) throws IOException {
        return grab(book, verseStr, "", true);
    }

    public static String grab(String book, String verseStr, String chapterBreak, boolean fromS3Bucket) throws IOException {
        
        verseStr = StringUtils.trim(verseStr);
        List<String> result = new ArrayList<>();
        String chapterWord = TextUtil.returnChapterWord(book);
        if ( hasMultipleChapter(verseStr, chapterWord) ) {
            String hyphen = getHyphen(verseStr);
            String[] array = splitVerses( verseStr, List.of(hyphen, "到", "至") );

            List<String> list1 = returnVerses(book, toEndVerses(array[0], chapterWord));
            result.addAll(list1);

            List<String> list2 = new ArrayList<>();
            list2.addAll(returnVerses(book, fromStartVerses(array[1], chapterWord)));

            int startingChapter = toNumber(StringUtils.split(list1.get(0), ".")[0]);
            int endingChapter = toNumber(StringUtils.split(list2.get(0), ".")[0]);
            Locale chineseNumbers = new Locale("C@numbers=hans");
            com.ibm.icu.text.NumberFormat formatter =
                    com.ibm.icu.text.NumberFormat.getInstance(chineseNumbers);
            for (int i = startingChapter + 1; i < endingChapter; i++) {
                result.addAll(returnVerses(book, formatter.format(i) + chapterWord));
            }
            result.addAll(list2);

        } else {
            result.addAll(returnVerses(book, verseStr));
        }
        return attachBibleVerses(book, result, chapterBreak, fromS3Bucket);
    }

    private static String fromStartVerses(String verseStr, String chapterWord) {
        String result = verseStr;
        if( StringUtils.endsWith(result, chapterWord) ){
            result += "1-200节";
        }else if( StringUtils.endsWith(ZhConverterUtil.toSimple(result), "节") ){
            result = StringUtils.substring(verseStr, 0, verseStr.indexOf(chapterWord)+1)+"1-"+StringUtils.substring(verseStr, verseStr.indexOf(chapterWord)+1);
        }
        
        return result;
    }

    private static String toEndVerses(String verseStr, String chapterWord) {

        String result = StringUtils.replace(appendChapterWord(verseStr, chapterWord), "节", "-200节");
        if( !StringUtils.endsWith(result, chapterWord) && !StringUtils.endsWith(ZhConverterUtil.toSimple(result), "节") ){
            result += "-200节";
        }
        
        return result;
    }

    private static String appendChapterWord(String input, String chapterWord) {
        String result = input;
        if( !StringUtils.contains(result, chapterWord) ){
            return  result+chapterWord;
        }

        return result;
    }

    private static boolean hasMultipleChapter(String verseStr, String chapterWord) {

        if( StringUtils.countMatches(verseStr, chapterWord) >= 2
                && (containHyphen(verseStr) || verseStr.contains("到") || verseStr.contains("至")) ){
            return true;
        }

        int hyphenPos = indexOfHyphen(verseStr);
        int chapterWordPos = StringUtils.indexOf(verseStr, chapterWord);
        if( (containHyphen(verseStr) || verseStr.contains("到") || verseStr.contains("至"))
                && StringUtils.countMatches(verseStr, chapterWord) == 1
                && chapterWordPos > hyphenPos ){
            return true;
        }

        return false;
    }

    private static int indexOfHyphen(String verseStr) {
        if( containHyphen(verseStr) ){
            String hyphen = getHyphen(verseStr);
            return StringUtils.indexOf(verseStr, hyphen);
        }

        if( StringUtils.contains(verseStr, "到") ){
            return StringUtils.indexOf(verseStr, "到");
        }

        if( StringUtils.contains(verseStr, "至") ){
            return StringUtils.indexOf(verseStr, "至");
        }

        return -1;
    }

    private static String[] splitVerses(String verseStr, List<String> spliters) {

        for( String spliter : spliters ){
            if( StringUtils.isEmpty(spliter) ){
                continue;
            }
            if( verseStr.contains(spliter) ){
                return verseStr.split(spliter);
            }
        }

        return new String[]{};
    }

    private static String attachBibleVerses(String book, List<String> verses, String chapterBreak, boolean fromS3Bucket) throws IOException {
        StringBuffer buffer = new StringBuffer();
        String currentBookChapter = "";
        for (String verse : verses) {
            String[] array = StringUtils.split(verse, ".");
            int chapterNum = 0;
            int verseNum = 0;

            if (NumberUtils.isCreatable(StringUtils.trim(array[0]))) {
                chapterNum = NumberUtils.toInt(StringUtils.trim(array[0]));
            }
            if (array.length > 1 && NumberUtils.isCreatable(StringUtils.trim(array[1]))) {
                verseNum = NumberUtils.toInt(StringUtils.trim(array[1]));
            }

            if (!currentBookChapter.equals(book + chapterNum)) {
//                buffer.append(book).append(chapterNum);
//                if( book.equals("詩篇") ){
//                    buffer.append("篇");
//                }else{
//                    buffer.append("章");
//                }
                buffer
//                        .append("[pause]")
                        .append(System.lineSeparator());
                currentBookChapter = book + chapterNum;
            }

            if (verseNum == 0) {
                buffer.append(recurBibleVerse("", book, chapterNum, verseNum, chapterBreak, fromS3Bucket));
            } else {
                String result = grabBibleVerse(book, chapterNum, verseNum, fromS3Bucket);
                if (StringUtils.isEmpty(result)) {
                    continue;
                }
//                if( !StringUtils.containsAnyIgnoreCase(buffer.toString(), result) ){
                    buffer
//                        .append(verseNum).append("節")
//                        .append("[pause]")
                            .append(result).append(System.lineSeparator());
//                }
            }
        }

        String result = buffer.toString();
        return PunctuationTool.replacePauseTag(changeFullCharacter(result));
    }

    private static String recurBibleVerse(String grabResult, String book, int chapter, int verse, String chapterBreak, boolean fromS3Bucket) throws IOException {
        if (verse == 0) {
            return recurBibleVerse("", book, chapter, 1, chapterBreak, fromS3Bucket);
        }

        StringBuffer buffer = new StringBuffer(grabResult);
        String response = grabBibleVerse(book, chapter, verse, fromS3Bucket);
        if (StringUtils.isEmpty(response)) {
            if( !StringUtils.isEmpty(chapterBreak) ){
                return buffer.toString() + System.lineSeparator() + chapterBreak;
            }
            return buffer.toString();
        }
//        if( !StringUtils.containsAnyIgnoreCase(buffer.toString(), response) ){
            buffer
//                .append(verse).append("節")
//                .append("[pause]")
                    .append(response).append(System.lineSeparator());
//        }

        int i = verse + 1;
        return recurBibleVerse(buffer.toString(), book, chapter, i, chapterBreak, fromS3Bucket);
    }

    private static String grabBibleVerse(String book, int chapter, int verse, boolean fromS3Bucket) throws IOException {

//        String result = grabBibleVerseFromUrVersion(book, chapter, verse);
//        if( StringUtils.isEmpty(result) ){
//            result = grabBibleVerseFromEDZX(book, chapter, verse);
//        }
//        String result = grabBibleVerseFromEDZX(book, chapter, verse);
//        if( StringUtils.isEmpty(result) ){
//            result = grabBibleVerseFromBGW(book, chapter, verse);
//            if( StringUtils.equalsIgnoreCase(StringUtils.trim(result), "a") ){
//                result = PunctuationTool.getPauseTag(100);
//            }
//        }
        String result = grabBibleVerseFromBGW(book, chapter, verse, fromS3Bucket);

        return result;
    }

    private static String grabBibleVerseFromUrVersion(String book, int chapter, int verse) throws IOException {
        String bookChapter = book + chapter;
        String text = "";
        Properties bookMapping = AppProperties.readurVersionBibleMapping();
        if (textCacheUrVersion.containsKey(bookChapter)) {
            text = textCacheUrVersion.get(bookChapter);
        } else {
            text = readFromUrVersion(bookMapping.getProperty(ZhConverterUtil.toSimple(book)), chapter);
            textCacheUrVersion.put(bookChapter, text);
        }

        List<String> versePatterns = createVersePatternUrVersion(bookMapping.getProperty(ZhConverterUtil.toSimple(book)), chapter, verse);
        String result = "";
        for( String versePattern : versePatterns ){
            Pattern p = Pattern.compile(versePattern);
            Matcher matcher = p.matcher(text);
            if (!matcher.find()) {
                continue;
            }
            int groupId = 1;
            logger.info(matcher.group(groupId));
            result = ZhConverterUtil.toSimple(StringUtils.remove(removeHtmlTag(matcher.group(groupId)), " "));
            result = result.replaceAll("^\\d+[-\\d]{0,}", "");
        }
        return StringUtils.trim(result);
    }

    private static String grabBibleVerseFromBGW(String book, int chapter, int verse, boolean fromS3Bucket) throws IOException {
        String bookChapter = book + chapter;
        String text = "";

        if( fromS3Bucket ){
            text = AWSUtil.readS3Object(AppProperties.getConfig().getProperty("bible_script_bucket"),
                    AppProperties.getConfig().getProperty("bible_script_prefix")
                            + ZhConverterUtil.toTraditional(book) +"/"+chapter+"/"+verse+"."+AppProperties.getConfig().getProperty("bible_script_format"));
        }
        if( !StringUtils.isEmpty(text) ){
            return extractVerse(text, book, chapter, verse);
        }

        if (textCacheBGW.containsKey(bookChapter)) {
            text = textCacheBGW.get(bookChapter);
        } else {
            text = readFromBGW(book, chapter);
            textCacheBGW.put(bookChapter, text);
        }

        String result = extractVerse(text, book, chapter, verse);

        return result;
    }

    private static String extractVerse(String input) {
        String regex = "<prosody[^>]*>(.*?)</prosody>";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
//            System.out.println(matcher.group(1));
            return StringUtils.replace(StringUtils.trim(matcher.group(1)), System.lineSeparator(), "");
        }

        return StringUtils.EMPTY;
//        return StringUtils.trim(TextUtil.removeXMLTag(input));
    }

    private static String extractVerse(String text, String book, int chapter, int verse) {
        if( StringUtils.startsWith(StringUtils.trim(text), "<speak") ){
            return extractVerse(text);
        }

        String versePattern = createVersePatternBGW(book, chapter, verse);
        Pattern p = Pattern.compile(versePattern);
        Matcher matcher = p.matcher(text);
        String result = "";
        int groupId = 2;
        if (matcher.find()) {
            logger.info(matcher.group(groupId));
            result = ZhConverterUtil.toSimple(StringUtils.remove(removeHtmlTag(matcher.group(groupId)), " "));
        }
        return result;
    }

    private static String grabBibleVerseFromEDZX(String book, int chapter, int verse) throws IOException {
        String bookChapter = book + chapter;
        String text = "";
        if (textCacheEDZX.containsKey(bookChapter)) {
            text = textCacheEDZX.get(bookChapter);
        } else {
            Properties bookMapping = AppProperties.readEDZXBibleMapping();
            text = readFromEDZX(Integer.valueOf(bookMapping.getProperty(ZhConverterUtil.toSimple(book))), chapter);
            textCacheEDZX.put(bookChapter, text);
        }

        String versePattern = createVersePatternEDZX(chapter, verse);
        Pattern p = Pattern.compile(versePattern);
        Matcher matcher = p.matcher(text);
        String result = "";
        int groupId = 1;
        if (matcher.find()) {
            logger.info(matcher.group(groupId));
            result = ZhConverterUtil.toSimple(StringUtils.remove(removeHtmlTag(matcher.group(groupId)), " "));
        }

        return result;
    }

//    private static boolean verifyVerse(String text, int chapter, int verse, boolean isBGW) {
//        String versePattern = createVersePatternEDZX(chapter, verse);
//        if( isBGW ){
//            versePattern = createVersePatternBGW(chapter, verse);
//        }
//        Pattern p = Pattern.compile(versePattern);
//        Matcher matcher = p.matcher(text);
//        return matcher.find();
//    }

    /*
    <td>8:21</td>
   <td>
      <div class="verse_list">                                    西巴和撒慕拿说，你自己起来杀我们吧。因为人如何，力量也是如何。基甸就起来，杀了西巴和撒慕拿，夺获他们骆驼项上戴的月牙圈。                                </div>
   </td>
     */
    //約翰二書 腓利門書 猶大書  俄巴底亞書   約翰三書
    private static final List<String> oneChapterBooks = Arrays.asList("約翰二書", "腓利門書", "猶大書", "俄巴底亞書", "約翰三書");
    private static String createVersePatternBGW(String book, int chapterNumber, int verseNumber) {
        if (verseNumber == 1 && !oneChapterBooks.contains(book)) {
            return "(<span\\s{1,}class=\"chapternum\">" + chapterNumber + "[\\u00A0|&nbsp;|\\s]</span>)([^<>]*)(</span>)";
        }

        return "(<sup\\s{1,}class=\"versenum\">" + verseNumber + "[\\u00A0|&nbsp;|\\s]</sup>)([^<>]*)(</span>)";
    }

    private static String createVersePatternEDZX(int chapterNumber, int verseNumber) {
        return "<td>"+chapterNumber+":"+verseNumber+"</td>\\s*<td>\\s*<div\\s+class=\"verse_list\">\\s*([^<]+)\\s*</div>\\s*</td>";
    }
    
    private static List<String> createVersePatternUrVersion(String bookAbbre, int chapterNumber, int verseNumber) {
        List<String> patterns = new ArrayList<>();
        String combination = bookAbbre+"."+chapterNumber+"."+verseNumber;
        String nextCombination = bookAbbre+"."+chapterNumber+"."+(verseNumber+1);
        String next2Combination = bookAbbre+"."+chapterNumber+"."+(verseNumber+2);
        patterns.add("<span data-usfm=\\\""+combination+"\\\" class=\\\"ChapterContent_verse__57FIw\\\">(.*?)<span data-usfm=\\\""+nextCombination);
        patterns.add("<span data-usfm=\\\""+combination+"\\+"+nextCombination+"\\\" class=\\\"ChapterContent_verse__57FIw\\\">(.*?)<span data-usfm=\\\""+next2Combination);
        return patterns;
    }

    private static String readFromInternet(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        int responseCode = con.getResponseCode();
//        System.out.println("Response code: " + responseCode);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        String html = response.toString();
//        System.out.println(html);

        return html;
    }

    private static String readFromUrVersion(String bookName, int chapterNumber) throws IOException {

        String url = "https://www.bible.com/bible/46/"+bookName+"."+chapterNumber+".CUNP-%25E7%25A5%259E";
        String html = readFromInternet(url);
        return html;
    }

    private static String readFromEDZX(int bookNumber, int chapterNumber) throws IOException {

        String url = "http://www.edzx.com/bible/read/?id=1&volume="+bookNumber+"&chapter="+chapterNumber;
        String html = readFromInternet(url);
        return html;
    }
    private static String readFromBGW(String book, int chapterNumber) throws IOException {

        String url = "https://www.biblegateway.com/passage/?search=" + URLEncoder.encode(book+chapterNumber) + "&version="+ AppProperties.getConfig().getProperty("bible_version");
        String html = readFromInternet(url);
        return html;
    }

    private static List<String> returnVerses(String book, String verseStr) {
        int chapter = 0;
        String chapterWord = TextUtil.returnChapterWord(book);
        if (verseStr.contains(chapterWord)) {
            chapter = toNumber(StringUtils.substring(verseStr, 0, verseStr.indexOf(chapterWord)));
        }
       
        String verses = "";
        if (verseStr.contains(chapterWord)) {
            verses = StringUtils.substring(verseStr, verseStr.indexOf(chapterWord) + 1);
        }
       
        if (verses.contains("节") || verses.contains("節")) {
//            int index = (verses.endsWith("节")) ? verses.indexOf("节") : (verses.endsWith("節")) ? verses.indexOf("節") : 0;
//            verses = StringUtils.substring(verses, 0, index);
            verses = StringUtils.replace(verses, "节", "").replace("節", "");
        }

        List<String> result = new ArrayList<>();
        if ((containHyphen(verses) || verses.contains("到") || verses.contains("至"))) {
            String hyphen = getHyphen(verses);
            String[] array = splitVerses( verses, List.of(hyphen, "到", "至") );
            int start = toNumber(array[0]);
            int end = toNumber(array[1]);

            for (int i = start; i <= end; i++) {
                result.add(chapter + "." + i);
            }
        } else if (verses.contains(",")) {
            String array[] = verses.split(",");
            for (String verse_ind : array) {
                result.add(chapter + "." + toNumber(verse_ind));
            }
        } else {
            if( StringUtils.isEmpty(verses) ){
                result.add(chapter + ".");
            }else{
                result.add(chapter + "." + toNumber(verses));
            }
        }

        return result;
    }

}
