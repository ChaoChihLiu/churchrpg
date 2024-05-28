package com.cpbpc.rpgv2.en;


import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.PunctuationTool;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.TextUtil.removeHtmlTag;


public class BibleVerseGrab {

    private static final String[] hyphens_unicode = new String[]{"\\u002d", "\\u2010", "\\u2011", "\\u2012", "\\u2013", "\\u2015", "\\u2212"};
    private static Map<String, String> textCache = new HashMap<>();

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
        if (NumberUtils.isCreatable(s)) {
            return NumberUtils.createInteger(s);
        }

        return 0;
    }

    public static void main(String[] args) throws IOException {
//                String verseStr = "32:7";
//        String verseStr = "31:1,7, 9,11";
//        String verseStr = "3:7-9";
        String book = "Leviticus";
//        String book = "Proverbs";
//        String verseStr = "32:7-34";
//        String verseStr = "32:7-34:9";
//        String verseStr = "24:1-8";
//        String verseStr = "24-26:8";                  
//        String verseStr = "9";
//        String verseStr = "25:23-28, 47-55";
        String verseStr = "25:23-28, 47,49,51";
        System.out.println(grab(book, verseStr));
    }
    public static String grab(String book, String verseStr) throws IOException {
        return grab(book, verseStr, "");
    }

    public static String grab(String book, String verseStr, String chapterBreak) throws IOException {
        book = convertOrdinalNumber(book);

        //25:23-28, 47-55
        //25:23-28, 47,49,51
        if (checkCondition3(verseStr)) {
            List<String> result = new ArrayList<>();
            result.addAll(returnVerses(verseStr));
            return attachBibleVerses(book, result, chapterBreak);
        }
        //3:7-9
        //32:7-34:9
        else if (checkCondition1(verseStr)) {
            List<String> result = new ArrayList<>();
            result.addAll(returnVerses(verseStr));
            return attachBibleVerses(book, result, chapterBreak);
        }
        //3-9
        else if (checkCondition4(verseStr)) {
            String hyphen = getHyphen(verseStr);
            String[] array = StringUtils.split(verseStr, hyphen);
            StringBuilder builder = new StringBuilder();
            int startingChapter = toNumber(StringUtils.split(array[0], ".")[0]);
            int endingChapter = toNumber(StringUtils.split(array[1], ".")[0]);
            for (int i = startingChapter; i <= endingChapter; i++) {
                List<String> result = new ArrayList<>();
                result.addAll(returnVerses(i + ":1-200"));
                builder.append(attachBibleVerses(book, result, chapterBreak)).append(chapterBreak);
            }
            return builder.toString();
        }
        //24-26:8
        else if (checkCondition4(verseStr)
                || checkCondition2(verseStr)) {
            List<String> result = new ArrayList<>();
            String hyphen = getHyphen(verseStr);
            String[] array = StringUtils.split(verseStr, hyphen);

            List<String> list1 = new ArrayList<>();
            if (StringUtils.contains(array[0], ":")) {
                list1.addAll(returnVerses(array[0] + "-200"));
            } else {
                list1.addAll(returnVerses(array[0]));
            }

            List<String> list2 = new ArrayList<>();
            if (StringUtils.contains(array[1], ":")) {
                String[] sub = array[1].split(":");
                list2.addAll(returnVerses(sub[0] + ":1-" + sub[1]));
            } else {
                list2.addAll(returnVerses(array[1]));
            }
            int startingChapter = toNumber(StringUtils.split(list1.get(0), ".")[0]);
            int endingChapter = toNumber(StringUtils.split(list2.get(0), ".")[0]);
            result.addAll(list1);
            for (int i = startingChapter + 1; i < endingChapter; i++) {
                result.addAll(returnVerses(i + ""));
            }
            result.addAll(list2);

        }
        //only has 1 but entire chapter
        else if( NumberUtils.isCreatable(StringUtils.trim(verseStr)) ){
            List<String> result = new ArrayList<>();
            result.addAll(returnVerses(verseStr+ ":1-200"));
            return attachBibleVerses(book, result, chapterBreak);
        }else {
            List<String> result = new ArrayList<>();
            result.addAll(returnVerses(verseStr));
            return attachBibleVerses(book, result, chapterBreak);
        }

        return "";
    }

    //25:23-28, 47-55
    //25:23-28, 47,49,51
    private static boolean checkCondition3(String verseStr) {
        StringBuilder pattern_str = new StringBuilder("[0-9]{1,3}:[0-9]{1,3}");
        pattern_str.append("[");
        String[] hyphens = PunctuationTool.getHyphensUnicode();
        for( String hyphen : hyphens ){
            pattern_str.append(hyphen);
        }
        pattern_str.append("][0-9]{1,3},\\s{1,}[0-9]{1,3}");

        Pattern patter3 = Pattern.compile(pattern_str.toString());
        Matcher matcher = patter3.matcher(verseStr);
        if( matcher.find() ){
            return true;
        }
        return false;
    }

    //24-26:8
    private static boolean checkCondition2(String verseStr) {
        StringBuilder pattern_str = new StringBuilder("[0-9]{1,3}");
        pattern_str.append("[");
        String[] hyphens = PunctuationTool.getHyphensUnicode();
        for( String hyphen : hyphens ){
            pattern_str.append(hyphen);
        }
        pattern_str.append("][0-9]{1,3}:[0-9]{1,3}");

        Pattern patter2 = Pattern.compile(pattern_str.toString());
        Matcher matcher = patter2.matcher(verseStr);
        if( matcher.find() ){
            return true;
        }
        return false;
    }

    //3:7-9
    //32:7-34:9
    private static boolean checkCondition1(String verseStr) {
        StringBuilder pattern_str = new StringBuilder("[0-9]{1,3}:[0-9]{1,3}");
        pattern_str.append("[");
        String[] hyphens = PunctuationTool.getHyphensUnicode();
        for( String hyphen : hyphens ){
            pattern_str.append(hyphen);
        }
        pattern_str.append("][0-9]{1,3}:{0,}[0-9]{0,3}");

        Pattern patter1 = Pattern.compile(pattern_str.toString());
        Matcher matcher = patter1.matcher(verseStr);
        if( matcher.find() ){
            return true;
        }
        return false;
    }
    //3-9
    private static boolean checkCondition4(String verseStr) {
        StringBuilder pattern_str = new StringBuilder("[0-9]{1,3}");
        pattern_str.append("[");
        String[] hyphens = PunctuationTool.getHyphensUnicode();
        for( String hyphen : hyphens ){
            pattern_str.append(hyphen);
        }
        pattern_str.append("][0-9]{1,3}");

        Pattern patter1 = Pattern.compile(pattern_str.toString());
        Matcher matcher = patter1.matcher(verseStr);
        if( matcher.find() ){
            return true;
        }
        return false;
    }

    private static String convertOrdinalNumber(String book) {

        if( StringUtils.startsWithIgnoreCase(book, "first") ){
            return StringUtils.replaceIgnoreCase(book, "first", "1");
        }
        if( StringUtils.startsWithIgnoreCase(book, "second") ){
            return StringUtils.replaceIgnoreCase(book, "second", "2");
        }
        if( StringUtils.startsWithIgnoreCase(book, "third") ){
            return StringUtils.replaceIgnoreCase(book, "third", "3");
        }

        return book;
    }

    private static String attachBibleVerses(String book, List<String> verses, String chapterBreak) throws IOException {
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

//                if( !StringUtils.equalsIgnoreCase("Psalms", book) ){
//                    buffer.append(book).append(" chapter ").append(chapterNum);
//                }else{
//                    buffer.append(book).append(" ").append(chapterNum);
//                }
                buffer
//                        .append("[pause]")
                        .append(System.lineSeparator());
                currentBookChapter = book + chapterNum;
            }

            if (verseNum == 0) {
                buffer.append(recurBibleVerse("", book, chapterNum, verseNum, chapterBreak));
            } else {
                String result = grabBibleVerse(book, chapterNum, verseNum);
                if (StringUtils.isEmpty(result)) {
                    continue;
                }
                buffer
//                        .append("verse ").append(verseNum)
//                        .append("[pause]")
                        .append(result).append(System.lineSeparator());
            }
        }

        return buffer.toString();
    }

    private static String recurBibleVerse(String grabResult, String book, int chapter, int verse, String chapterBreak) throws IOException {
        if (verse == 0) {
            return recurBibleVerse("", book, chapter, 1, chapterBreak);
        }

        StringBuffer buffer = new StringBuffer(grabResult);
        String response = grabBibleVerse(book, chapter, verse);
        if (StringUtils.isEmpty(response)) {
            if( !StringUtils.isEmpty(chapterBreak) ){
                return buffer.toString() + System.lineSeparator()+chapterBreak;
            }
            return buffer.toString();
        }
        buffer
//                .append("verse ").append(verse)
//                .append("[pause]")
                .append(response).append(System.lineSeparator());

        int i = verse + 1;
        return recurBibleVerse(buffer.toString(), book, chapter, i, chapterBreak);
    }

    private static String grabBibleVerse(String book, int chapter, int verse) throws IOException {
        String bookChapter = book + chapter;
        String text = "";
        if (textCache.containsKey(bookChapter)) {
            text = textCache.get(bookChapter);
        } else {
            text = readFromInternet(bookChapter);
            textCache.put(bookChapter, text);
        }

        return extractVerse(text, chapter, verse);
    }

    /*
    <p class="chapter-2"><span id="en-KJV-14333" class="text Ps-31-1"><span class="chapternum">31&nbsp;</span>In thee, O <span style="font-variant: small-caps" class="small-caps">Lord</span>, do I put my trust; let me never be ashamed: deliver me in thy righteousness.</span></p>testtesttesttest
    <span id="en-KJV-14341" class="text Ps-31-9"><sup class="versenum">9&nbsp;</sup>Have mercy upon me, O <span style="font-variant: small-caps" class="small-caps">Lord</span>, for I am in trouble: mine eye is consumed with grief, yea, my soul and my belly.</span>
    <span id="en-KJV-14343" class="text Ps-31-11"><sup class="versenum">11&nbsp;</sup>I was a reproach among all mine enemies, but especially among my neighbours, and a fear to mine acquaintance: they that did see me without fled from me.</span>
     */
    private static String extractVerse(String input, int chapterNumber, int verseNumber) {
        if (verseNumber == 1) {
            return extractVerse(input, "<span\\s{1,}class=\"chapternum\">" + chapterNumber + "\\u00A0</span>", "</span></p>");
        }

        return extractVerse(input, "<sup\\s{1,}class=\"versenum\">" + verseNumber + "\\u00A0</sup>", "</span></p>");
    }

    private static String extractVerse(String input, String startAnchor, String endAnchor) {
        Pattern p = Pattern.compile(startAnchor);
        Matcher m = p.matcher(input);
        int start = 0;
        int end = 0;
        if (m.find(start)) {
            start = m.end();
        }

        p = Pattern.compile(endAnchor);
        m = p.matcher(input);
        if (m.find(start)) {
            end = m.start();
        }
        if (start == 0) {
            return "";
        }

        return removeHtmlTag(input.substring(start, end));
    }

    private static String readFromInternet(String bookChapter) throws IOException {

        String url = "https://www.biblegateway.com/passage/?search=" + URLEncoder.encode(bookChapter) + "&version="+AppProperties.getConfig().getProperty("bible_version");
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

    private static List<String> returnVerses(String verseStr) {
        int chapter = 0;
        if (!verseStr.contains(":") && !containHyphen(verseStr)) {
            chapter = toNumber(verseStr);
        }
        if (verseStr.contains(":")) {
            chapter = toNumber(StringUtils.substring(verseStr, 0, verseStr.indexOf(":")));
        }

        String verses = "";
        if (verseStr.contains(":")) {
            verses = StringUtils.substring(verseStr, verseStr.indexOf(":") + 1);
        }

        List<String> result = new ArrayList<>();
        if (containHyphen(verses) && verses.contains(",")) {
            String[] array = verses.split(",");
            for( String splitted : array ){
                splitted = StringUtils.trim(splitted);
                if(NumberUtils.isCreatable(splitted)){
                    result.add(chapter + "." + splitted);
                }

                if( containHyphen(splitted) ){
                    String hyphen = getHyphen(splitted);
                    String[] nums = splitted.split(hyphen);
                    int start = toNumber(nums[0]);
                    int end = toNumber(nums[1]);

                    for (int i = start; i <= end; i++) {
                        result.add(chapter + "." + i);
                    }
                }
            }

        }
        if (containHyphen(verses) && !verses.contains(",")) {
            String hyphen = getHyphen(verses);
            String[] array = verses.split(hyphen);
            int start = toNumber(StringUtils.trim(array[0]));
            int end = toNumber(StringUtils.trim(array[1]));

            for (int i = start; i <= end; i++) {
                result.add(chapter + "." + i);
            }
        }
        if (verses.contains(",") && !containHyphen(verses)) {
            String array[] = verses.split(",");
            for (String verse_ind : array) {
                result.add(chapter + "." + StringUtils.trim(verse_ind));
            }
        }
        if (NumberUtils.isCreatable(verses)) {
            result.add(chapter + "." + verses);
        }

        return result;
    }

}
