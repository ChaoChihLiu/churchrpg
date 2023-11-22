package com.cpbpc.rpgv2.en;

import com.cpbpc.rpgv2.ConfigObj;
import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerseRegExp implements VerseIntf {

    private static final String[] hyphens_unicode = new String[]{"\\u002d", "\\u2010", "\\u2011", "\\u2012", "\\u2013", "\\u2015", "\\u2212"};
    private static final Map<String, ConfigObj> verse = new HashMap();
    private static Logger logger = Logger.getLogger(VerseRegExp.class.getName());

    public void put(String shortForm, String completeForm, boolean isPaused) {
        verse.put(shortForm, new ConfigObj(shortForm, completeForm, false));
    }

    public Map<String, ConfigObj> getVerseMap(){
        return verse;
    }

    //    public static void main( String[] args ){
    public String convert(String content) throws IOException {

        String replaced = content;

        replaced = analyseCharByChar(content);
        replaced = replaceSingleVerseRef(replaced);

        logger.info("replaced : " + replaced);
        return replaced;
    }

    /*
    need to replace (v16) to be (version 16)
    (v16-20) to be (version 16 to 20)
     */
//    private static String SingleVersePattern = "\\(\\s{0,}[Vv]{1,2}\\.{0,}\\s{0,}[0-9]{1,3}[‑,-]{0,}\\s{0,}[0-9]{0,3}\\s{0,}\\)";
    private static String genSingleVersePattern() {
        String singleVersePattern = "\\(\\s{0,}[Vv]{1,2}\\.{0,}\\s{0,}[0-9]{1,3}[,";

        for (String hyphen_unicode : hyphens_unicode) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            singleVersePattern += hyphen;
        }
        singleVersePattern += "]{0,}\\s{0,}[0-9]{0,3}\\s{0,}\\)";

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
            while (replaced.contains(finding)) {
                String replacement = finding.toLowerCase()
                        .replace(".", "");
                for (String hyphen_unicode : hyphens_unicode) {
                    String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
                    replacement = replacement.replace(hyphen, " to ");
                }
                if (!replacement.toLowerCase().contains("verse") && finding.toLowerCase().contains("vv")) {
                    replacement = replacement.replace("vv", "verses");
                }
                if (!replacement.toLowerCase().contains("verse") && finding.toLowerCase().contains("v")) {
                    replacement = replacement.replace("v", "verse");
                }
                replaced = replaced.replaceFirst(finding.replace("(", "\\(")
                                .replace(")", "\\)"),
                        replacement
                );

            }
        }

        return replaced;
    }

    private static String analyseCharByChar(String content) throws IOException {

        String p = generateVersePattern();
        String replaced = content;
        logger.info(p);

        Pattern r = Pattern.compile(p);
        Matcher matcher = r.matcher(replaced);
        Map<String, String> finds = new LinkedHashMap<>();
        while (matcher.find()) {
            finds.put(matcher.group(0).trim(), matcher.group(2).trim());
        }
        logger.info("what is my finds : " + finds.toString());

        Set<Map.Entry<String, String>> entries = finds.entrySet();
        int anchorPoint = find1stPragraph(content);
        int counter = 0;
        for (Map.Entry<String, String> entry : entries) {

            while (replaced.contains(entry.getKey())) {
                String ref = appendNextCharTillCompleteVerse(replaced, entry.getKey(), 0);
                int position = content.indexOf(ref) + ref.length();
                String book = StringUtils.trim(entry.getValue());
                String verse_str = StringUtils.trim(ref.replaceFirst(book, ""));
                String replace = generateCompleteVerses(mapBookAbbre(book), verse_str);
                //put full bible verse in
                if (position <= anchorPoint) {
                    counter++;
                    String scraped = BibleVerseScraper.scrap(book, verse_str);
//                    The first Bible passage for today is
                    replace = "The " + ordinal(counter) + " Bible passage for today is " + replace + "[pause]" + scraped;
                }
//                logger.info( "book is " + book + ", verses is " + verse_str );
                logger.info("ref is " + ref + ", replace is " + replace);
                replaced = replaced.replaceFirst(ref, replace);
            }

        }


        return replaced;
//        int newStart = replaced.indexOf(replace)+replace.length();
//        logger.info( "new start is " + newStart );
//        return analyseCharByChar(replaced, newStart);
    }

    public static String ordinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + suffixes[i % 10];
        }
    }

    private static int find1stPragraph(String content) {
        return content.indexOf("<p>&nbsp;</p>");
    }

    private static String appendNextCharTillCompleteVerse(String content, String ref, int startFrom) {
        if (content == null || content.trim().length() <= 0 || ref == null || ref.trim().length() <= 0) {
            return "";
        }

        int position = content.indexOf(ref, startFrom) + ref.length();
        StringBuilder builder = new StringBuilder(ref);
        List<String> verseParts = new ArrayList<>();
        List<String> punctuations = new ArrayList<>();
        punctuations.addAll(List.of(":", ",", " ", ";"));
        for (String hyphen_unicode : hyphens_unicode) {
            punctuations.add(StringEscapeUtils.unescapeJava(hyphen_unicode));
        }

        verseParts.addAll(punctuations);
        for (int i = 0; i < 10; i++) {
            verseParts.add(String.valueOf(i));
        }
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

    private static String generateVersePattern() {
        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = verse.keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(key.toString()).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append(")[.]{0,1}\\s{0,}[0-9]{1,3})");

        return builder.toString();
    }

    private static String analyseWithMultiReg(String content) throws UnsupportedEncodingException {

        List<String> ps = generateVersePatterns();
        String replaced = content;
        for (String p : ps) {
            logger.info(p);

            Pattern r = Pattern.compile(p);
            Matcher matcher = r.matcher(content);
            int start = 0;
            while (matcher.find(start)) {
                logger.info("0 is " + matcher.group(0).trim());
                logger.info("1 is " + matcher.group(1).trim());

                String ref = matcher.group(0).trim();
                String book = matcher.group(1).trim();
                String verse_str = ref.replaceFirst(book, "");
                String replace = generateCompleteVerses(mapBookAbbre(book), verse_str);
                logger.info("ref is " + ref + ", replace is " + replace);
                replaced = replaced.replaceFirst(ref, replace);

                start = matcher.end();
            }

            logger.info("replaced : " + replaced);
        }

        return replaced;

    }

    private static List<String> generateVersePatterns() {

        List<String> list = new ArrayList<>();

        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = verse.keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|");
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append(")[.]{0,1}\\s{0,})");

        list.add(generateMultiVerseAndConsecutivePattern(builder.toString()));
        list.add(generateConsecutiveVersePattern(builder.toString()));
        list.add(generateMultiVersePattern(builder.toString()));
        list.add(generateSingleVersePattern(builder.toString()));
        list.add(generateConsecutiveBookPattern(builder.toString()));

        return list;
    }

    //Gen 1:3
    //1 Cor1:2
    private static String generateSingleVersePattern(String bookPattern) {
        return new StringBuilder(bookPattern).append("([0-9]{1,3}:[0-9]{1,3})").toString();
    }

    //Gen 1-3
    //Gen1-3
    private static String generateConsecutiveBookPattern(String bookPattern) {
        return new StringBuilder(bookPattern).append("([0-9]{1,3}\\-[0-9]{1,3})").toString();
    }

    private static String generateMultiVerseAndConsecutivePattern(String bookPattern) {
        return new StringBuilder(bookPattern).append("([0-9:,]{1,})").toString();
    }

    //Gen 1:3-2:1
    //Gen 1:3-14
    private static String generateConsecutiveVersePattern(String bookPattern) {
        return new StringBuilder(bookPattern).append("([0-9]{1,3}:[0-9]{1,3}-[0-9]{0}:{0}[0-9]{1,3})").toString();
    }

    //Gen 1:3,4,5,6
    private static String generateMultiVersePattern(String bookPattern) {
        return new StringBuilder(bookPattern).append("(([0-9]{1,3}:([0-9]){1,3},{0,}){1,})").toString();
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
                    completeVerse.append(convertVerse(verse.trim()));
                    if (verses.length - 1 > count) {
                        completeVerse.append(" to ");
                        if (!book.toLowerCase().equals("psalms")) {
                            completeVerse.append(" chapter ");
                        }
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
                    completeVerse.append(convertVerse(verse.trim()));
                    if (verses.length - 1 > count) {
                        completeVerse.append("; ");
                        if (!book.toLowerCase().equals("psalms")) {
                            completeVerse.append(" chapter ");
                        }
                    }
                    count++;
                }
                return completeVerse
//                        .append(",")
                        .toString();
            }
        }

        if (verse_str.trim().length() <= 1) {
            return book + " chapter " + verse_str;
        }

        if (!book.toLowerCase().equals("psalms")) {
            return book + " chapter " + convertVerse(verse_str.trim()) + " ";
        }
        return book + " " + convertVerse(verse_str.trim()) + " ";
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

    private static String convertVerse(String verse) throws UnsupportedEncodingException {
        logger.info(verse);

        String end = "";
        if (verse.trim().endsWith(":") || endWithHyphen(verse)) {
            end = verse.trim().substring(verse.trim().length() - 1, verse.trim().length());
            verse = verse.trim().substring(0, verse.trim().length() - 1);
            logger.info("remove verse end " + verse);
        }

        String result = "";
        result = verse.replaceAll(":", " verse ");
        for (String hyphen_unicode : hyphens_unicode) {
            result = result.replaceAll(StringEscapeUtils.unescapeJava(hyphen_unicode), " to ");
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

    private static String mapBookAbbre(String book) {

        if (null == book || book.trim().length() <= 0) {
            return book;
        }

        book = book.replace(".", "").replace("&nbsp;", " ");

        if (verse.containsKey(book.trim())) {
            return "";
        }

        return book;
    }

}
