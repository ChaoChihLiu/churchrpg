package com.cpbpc.rpg;


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
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ZhBibleVerseScraper {

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

        String x = " 一二三四五六七八九十百";
        int l = s.length(),
                i = x.indexOf(s.charAt(l - 1)),
                j = x.indexOf(s.charAt(0));
        if (l < 2) return i; // 1-10
        if (l < 3) {
            if (i == 10) return j * 10; // 20,30,40,50,60,70,80,90
            if (i > 10) return j * 100; // 100,200,300,400,500,600,700,800,900
            return 10 + i; // 11-19
        }
        if (l < 4) return j * 10 + i; // 21-29,31-39,41-49,51-59,61-69,71-79,81-89,91-99
        if (l < 5) return j * 100 + i; // 101-109,201-209,301-309,401-409,501-509,601-609,701-709,801-809,901-909
        return j * 100 + i + x.indexOf(s.charAt(2)) * 10; // 111-119,121-129,131-139,...,971-979,981-989,991-999
    }

    public static void main(String[] args) throws IOException {
        //        String verseStr = "三十二篇7-9节";
//        String verseStr = "31篇7,11节";
//        String verseStr = "三章7-9节";
        String book = "詩篇";
        String verseStr = "三十二篇1节-三十六篇";
//        String verseStr = "三十四篇7-9节";
//        String verseStr = "三十四篇";
//        String verseStr = "三十四篇-三十六篇";
        System.out.println(scrap(book, verseStr));
    }

    public static String scrap(String book, String verseStr) throws IOException {
        List<String> result = new ArrayList<>();
        if ((StringUtils.countMatches(verseStr, "篇") >= 2
                || StringUtils.countMatches(verseStr, "章") >= 2) && containHyphen(verseStr)) {
            String hyphen = getHyphen(verseStr);
            String[] array = StringUtils.split(verseStr, hyphen);

            List<String> list1 = returnVerses(StringUtils.replace(array[0], "节", "-200节"));
            List<String> list2 = new ArrayList<>();
            String chapterWord = "章";
            if (StringUtils.contains(array[1], "篇")) {
                chapterWord = "篇";
            }
            list2.addAll(returnVerses(array[1]));

            int startingChapter = toNumber(StringUtils.split(list1.get(0), ".")[0]);
            int endingChapter = toNumber(StringUtils.split(list2.get(0), ".")[0]);
            result.addAll(list1);
            Locale chineseNumbers = new Locale("C@numbers=hans");
            com.ibm.icu.text.NumberFormat formatter =
                    com.ibm.icu.text.NumberFormat.getInstance(chineseNumbers);
            for (int i = startingChapter + 1; i < endingChapter; i++) {
                result.addAll(returnVerses(formatter.format(i) + chapterWord));
            }
            result.addAll(list2);

        } else {
            result.addAll(returnVerses(verseStr));
        }
        return attachBibleVerses(book, result);
    }

    private static String attachBibleVerses(String book, List<String> verses) throws IOException {
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
                buffer.append(recurBibleVerse("", book, chapterNum, verseNum));
            } else {
                String result = grabBibleVerse(book, chapterNum, verseNum);
                if (StringUtils.isEmpty(result)) {
                    continue;
                }
                buffer
//                        .append(verseNum).append("節")
                        .append("[pause]")
                        .append(result).append(System.lineSeparator());
            }
        }

        return buffer.toString();
    }

    private static String recurBibleVerse(String grabResult, String book, int chapter, int verse) throws IOException {
        if (verse == 0) {
            return recurBibleVerse("", book, chapter, 1);
        }

        StringBuffer buffer = new StringBuffer(grabResult);
        String response = grabBibleVerse(book, chapter, verse);
        if (StringUtils.isEmpty(response)) {
            return buffer.toString();
        }
        buffer
//                .append(verse).append("節")
                .append("[pause]")
                .append(response).append(System.lineSeparator());

        int i = verse + 1;
        return recurBibleVerse(buffer.toString(), book, chapter, i);
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

        String versePattern = createVersePattern(chapter, verse);

        Pattern p = Pattern.compile(versePattern);
        Matcher matcher = p.matcher(text);
        if (matcher.find()) {
//            System.out.println(matcher.group(2));
            return StringUtils.remove(matcher.group(2), " ").replaceAll("，", "[pause]").replaceAll("。", "[pause]");
        }

        return "";
    }

    private static String createVersePattern(int chapterNumber, int verseNumber) {
        if (verseNumber == 1) {
            return "(<span\\s{1,}class=\"chapternum\">" + chapterNumber + "\\u00A0</span>)([^<>]*)(</span></p>)";
        }

        return "(<sup\\s{1,}class=\"versenum\">" + verseNumber + "\\u00A0</sup>)([^<>]*)(</span></p>)";
    }

    private static String readFromInternet(String bookChapter) throws IOException {

        String url = "https://www.biblegateway.com/passage/?search=" + URLEncoder.encode(bookChapter) + "&version=CUV";
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
        if (verseStr.contains("章")) {
            chapter = toNumber(StringUtils.substring(verseStr, 0, verseStr.indexOf("章")));
        }
        if (verseStr.contains("篇")) {
            chapter = toNumber(StringUtils.substring(verseStr, 0, verseStr.indexOf("篇")));
        }

        String verses = "";
        if (verseStr.contains("章")) {
            verses = StringUtils.substring(verseStr, verseStr.indexOf("章") + 1);
        }
        if (verseStr.contains("篇")) {
            verses = StringUtils.substring(verseStr, verseStr.indexOf("篇") + 1);
        }
        if (verses.endsWith("节") || verses.endsWith("節")) {
            int index = (verses.endsWith("节")) ? verses.indexOf("节") : (verses.endsWith("節")) ? verses.indexOf("節") : 0;
            verses = StringUtils.substring(verses, 0, index);
        }

        List<String> result = new ArrayList<>();
        if (containHyphen(verses)) {
            String hyphen = getHyphen(verses);
            String[] array = verses.split(hyphen);
            int start = toNumber(array[0]);
            int end = toNumber(array[1]);

            for (int i = start; i <= end; i++) {
                result.add(chapter + "." + i);
            }
        } else if (verses.contains(",")) {
            String array[] = verses.split(",");
            for (String verse_ind : array) {
                result.add(chapter + "." + verse_ind);
            }
        } else {
            result.add(chapter + "." + verses);
        }

        return result;
    }

}
