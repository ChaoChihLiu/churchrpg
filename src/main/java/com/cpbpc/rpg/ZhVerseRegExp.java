package com.cpbpc.rpg;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZhVerseRegExp {

    private static final String[] hyphens_unicode = new String[]{"\\u002d", "\\u2010", "\\u2011", "\\u2012", "\\u2013", "\\u2015", "\\u2212"};
    private static final Map<String, String> abbre = new HashMap();
    private static Logger logger = Logger.getLogger(ZhVerseRegExp.class.getName());

    public static void put(String shortForm, String completeForm) {
        abbre.put(shortForm, completeForm);
    }

    //    public static void main( String[] args ){
    public static String convert(String content) throws IOException {

        String replaced = analyseTopicVerses(content);

        replaced = analyseCharByChar(replaced);
        replaced = replaceSingleVerseRef(replaced);

        logger.info("replaced : " + replaced);
        return replaced;
    }

    /*
    need to replace (v16) to be (version 16)
    (v16-20) to be (version 16 to 20)
     */
//    private static String SingleVersePattern = "\s{0,}[0-9]{1,3}\s{0,}[0-9]{0,3}\s{0,}";
    private static String genSingleVersePattern() {
        String singleVersePattern = "{0,}[0-9]{1,3}:[0-9]{1,3}";

        return singleVersePattern;
    }

    private static String replaceSingleVerseRef(String content) {

        String replaced = content;
        Pattern p = Pattern.compile(genSingleVersePattern());

        Matcher matcher = p.matcher(replaced);
        List<String> findings = new ArrayList<>();
        while (matcher.find()) {
            findings.add(matcher.group(0).trim());
        }
        logger.info("what is my finds : " + findings.toString());

        for (String finding : findings) {
            if (replaced.contains(finding)) {

                String replacement = "";
//                  if( !book.toLowerCase().equals("诗篇") ) {
//                      replacement = finding.replace( ":", " 章 " ) + " 節 ";
//                  }else{
//                      replacement = finding.replace( ":", " 篇 " ) + " 節 ";
//                  }
                replacement = finding.replace(":", " 章 ").replace("：", " 章 ") + " 節 ";
                replaced = replaced.replaceFirst(finding, replacement);

            }
        }

        return replaced;
    }

    private static String analyseTopicVerses(String content) throws IOException {

        String p = generateTopicVersePattern();
        String replaced = content;

        Pattern r = Pattern.compile(p);
        Matcher matcher = r.matcher(replaced);
        Map<String, String> finds = new LinkedHashMap<>();
        while (matcher.find()) {
            finds.put(matcher.group(0).trim(), matcher.group(2).trim());
        }
        logger.info("what is my finds : " + finds.toString());

        Set<Map.Entry<String, String>> entries = finds.entrySet();
        int counter = 0;
        for (Map.Entry<String, String> entry : entries) {
            counter++;
            if (replaced.contains(entry.getKey())) {
                String ref = appendNextCharTillCompleteVerse(replaced, entry.getKey(), 0);
                String book = entry.getValue();
                String verse_str = ref.replaceFirst(book, "");
                String verseContent = ZhBibleVerseScraper.scrap(book, verse_str);
                logger.info("bible versed scraped: " + verseContent);
                String replace = generateCompleteVerses(mapBookAbbre(book), verse_str);
                replace += appendCompleteBibleVerse(mapBookAbbre(book), verse_str);

                logger.info("ref is " + ref.replace(" ", "") + ", replace is " + "聖經經文第" + counter + "段[pause]" + verseContent);
                replaced = replaced.replaceFirst(ref, "聖經經文第" + counter + "段[pause]" + replace + "[pause]" + verseContent);
            }

        }

        return replaced;

    }

    private static String appendCompleteBibleVerse(String book, String verseStr) {
        String chapter = "";
        if (verseStr.contains("章")) {
            chapter = StringUtils.substring(verseStr, 0, verseStr.indexOf("章"));
        }
        if (verseStr.contains("篇")) {
            chapter = StringUtils.substring(verseStr, 0, verseStr.indexOf("篇"));
        }


        return "";
    }

    private static String analyseCharByChar(String content) throws UnsupportedEncodingException {

        String p = generateVersePattern();
        String replaced = content;

        Pattern r = Pattern.compile(p);
        Matcher matcher = r.matcher(replaced);
        Map<String, String> finds = new TreeMap<>();
        while (matcher.find()) {
            finds.put(matcher.group(0).trim(), matcher.group(2).trim());
        }
        logger.info("what is my finds : " + finds.toString());

        Set<Map.Entry<String, String>> entries = finds.entrySet();
//        int start = 0;
        for (Map.Entry<String, String> entry : entries) {
            screening:
            while (replaced.contains(entry.getKey())) {
                String ref_str = appendNextCharTillCompleteVerse(replaced, entry.getKey(), 0);
                if (ref_str.contains(";") || ref_str.contains(",")) {
                    String[] refs = new String[0];
                    if (ref_str.contains(";"))
                        refs = ref_str.split(";");
                    if (ref_str.contains(","))
                        refs = ref_str.split(",");
                    for (String ref : refs) {
                        String book = entry.getValue();
                        String verse_str = ref.replaceFirst(book, "");
                        String replace = generateCompleteVerses(mapBookAbbre(book), verse_str);
                        logger.info("ref is " + ref + ", replace is " + replace);
                        replaced = replaced.replaceFirst(ref, replace);
                    }
                } else {
                    String ref = ref_str;
                    String book = entry.getValue();
                    String verse_str = ref.replaceFirst(book, "");
                    String replace = generateCompleteVerses(mapBookAbbre(book), verse_str);

                    if (ref.replace(" ", "").equals(replace.replace(" ", ""))) {
                        break screening;
                    }

                    logger.info("ref is " + ref + ", replace is " + replace);
                    replaced = replaced.replaceFirst(ref, replace);
                }
            }
        }

        replaced = replaced.replaceAll(";", "[pause];");
        replaced = replaced.replaceAll(",", "[pause],");

        return replaced;

    }

    private static String appendNextCharTillCompleteVerse(String content, String ref, int startFrom) {
        if (content == null || content.trim().length() <= 0 || ref == null || ref.trim().length() <= 0) {
            return "";
        }

        int position = content.indexOf(ref, startFrom) + ref.length();
        StringBuilder builder = new StringBuilder(ref);
        List<String> verseParts = new ArrayList<>();
        List<String> punctuations = new ArrayList<>();
        punctuations.addAll(List.of(":", ",", " ", ";", "：", "，", "；"));
        for (String hyphen_unicode : hyphens_unicode) {
            punctuations.add(StringEscapeUtils.unescapeJava(hyphen_unicode));
        }

        verseParts.addAll(punctuations);
        for (int i = 0; i < 10; i++) {
            verseParts.add(String.valueOf(i));
        }
        verseParts.addAll(List.of("上", "下", "章", "篇", "节", "節", "到"));

        logger.info("verseParts : " + verseParts.toString());

        for (int i = position; i < content.length(); i++) {

            String nextChar = content.substring(i, i + 1);
            if (!verseParts.contains(nextChar)) {
                break;
            }
            builder.append(nextChar);
        }

        String result = builder.toString();
        if (punctuations.contains(result.substring(result.length() - 1, result.length()))) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private static String generateTopicVersePattern() {
        //雅各书一章1节    使徒行传十二章1-2节  哥林多后书6章14-7章1节
        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = abbre.keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(key.toString()).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append(")\\s{0,}[0-9一二三四五六七八九十百千零]{1,}\\s{0,}[章|篇])");

        logger.info(builder.toString());
        return builder.toString();
    }

    private static String generateVersePattern() {
        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = abbre.keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(key.toString()).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append(")\\s{0,}[0-9]{1,3}[:|：]{0,}\\){0,})");

        logger.info(builder.toString());
        return builder.toString();
    }

    private static String generateCompleteVerses(String book, String verse_str) throws UnsupportedEncodingException {
        if (null == verse_str || verse_str.trim().length() <= 0) {
            return book;
        }

        //Gen 1:3-4:10
        if (verse_str.trim().length() > 0
                &&
                containHyphen(verse_str)
                && verse_str.contains(":")
        ) {
            String[] verses = verse_str.split(getHyphen(verse_str));

            if (verses.length >= 2 && AllVersesContainChapter(verses)) {
                StringBuilder completeVerse = new StringBuilder(book + " ");
                int count = 0;
                for (String verse : verses) {
                    completeVerse.append(convertVerse(book, verse.trim()));
                    if (verses.length - 1 > count) {
                        completeVerse.append(" 到 ");
                    }
                    count++;
                }
                return completeVerse
//                        .append(",")
                        .toString();
            }
        }

        //Gen 1:3;4:10
        if (verse_str.trim().length() > 0 && verse_str.contains(";") && verse_str.contains(":")) {
            String[] verses = verse_str.split(";");
            if (verses.length >= 2 && AllVersesContainChapter(verses)) {
                StringBuilder completeVerse = new StringBuilder(book + " ");
                int count = 0;
                for (String verse : verses) {

                    completeVerse.append(convertVerse(book, verse.trim()));
                    if (verses.length - 1 > count) {
                        completeVerse.append("; ");
                    }
                    count++;
                }
                return completeVerse
//                        .append(",")
                        .toString();
            }
        }

        if (verse_str.trim().length() <= 1) {
            if (!book.toLowerCase().equals("诗篇")) {
                return book + " " + verse_str + " 章 ";
            }
            return book + " 篇 " + verse_str + " ";
        }

        return book + " " + convertVerse(book, verse_str.trim()) + " ";
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

    private static boolean containHyphen(String verseStr) {

        for (String hyphen_unicode : hyphens_unicode) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verseStr.contains(hyphen)) {
                return true;
            }
        }

        return false;
    }

    private static boolean AllVersesContainChapter(String[] verses) {

        for (String verse : verses) {
            if (!verse.contains(":")) {
                return false;
            }
        }

        return true;
    }

    private static String convertVerse(String book, String verse) throws UnsupportedEncodingException {
        logger.info("convertVerse: " + verse);

//        Locale chineseNumbers = new Locale("C@numbers=hans");
//        com.ibm.icu.text.NumberFormat formatter =
//                com.ibm.icu.text.NumberFormat.getInstance(chineseNumbers);
//
//        verse = formatter.format( Long.parseLong(verse) );

        String end = "";
        if (verse.trim().endsWith(":") || endWithHyphen(verse) || verse.trim().endsWith(";")) {
            end = verse.trim().substring(verse.trim().length() - 1, verse.trim().length());
            verse = verse.trim().substring(0, verse.trim().length() - 1);
            logger.info("remove verse end " + verse);
        }

        String result = "";
        if (!book.toLowerCase().equals("诗篇")) {
            result = verse.replaceAll(":", " 章 ").replaceAll("：", " 章 ");
        } else {
            result = verse.replaceAll(":", " 篇 ").replaceAll("：", " 篇 ");
        }

        // 犹15
        if ((!result.contains("章") && !result.contains("篇")) && NumberUtils.isCreatable(result.trim().substring(result.length() - 1, result.length()))) {
            result += "章";
        } else {
            //民22:7
            if (!result.trim().endsWith("節") && !result.trim().endsWith("节")
                    && NumberUtils.isCreatable(result.trim().substring(result.length() - 1, result.length()))) {
                result += " 節 ";
                //民22:7下
            } else if (result.trim().endsWith("上") || result.trim().endsWith("下")) {
                result = result.trim().substring(0, result.length() - 1) + "節" + result.trim().substring(result.length() - 1, result.length());
            } else {
                result += " ";
            }
        }

        for (String hyphen_unicode : hyphens_unicode) {
            result = result.replaceAll(StringEscapeUtils.unescapeJava(hyphen_unicode), " 到 ");
        }
        result += end;

        return result;
    }

    private static boolean endWithHyphen(String verse) {
        for (String hyphen_unicode : hyphens_unicode) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verse.endsWith(hyphen)) {
                return true;
            }
        }

        return false;
    }

    private static int toNumber(String s) {
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

    private static String mapBookAbbre(String book) {

        if (null == book || book.trim().length() <= 0) {
            return book;
        }

        book = book.replace(".", "");

        if (abbre.containsKey(book)) {
            return abbre.get(book);
        }

        return book;
    }

}
