package com.cpbpc.rpgv2.zh;

import com.cpbpc.comms.NumberConverter;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.ConfigObj;
import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.containHyphen;
import static com.cpbpc.comms.PunctuationTool.getAllowedPunctuations;

public class VerseRegExp implements VerseIntf {

    private static Logger logger = Logger.getLogger(VerseRegExp.class.getName());
    private final Pattern endWithVerseNumberPattern = Pattern.compile("\\d$");
    private final Pattern endWithPunctuationPattern = Pattern.compile("\\d[,|;|\\.]$");

    private Pattern versePattern = null;

    private static final Map<String, ConfigObj> verse = new HashMap();
    public void put(String shortForm, String completeForm, boolean isPaused) {
        verse.put(shortForm, new ConfigObj(shortForm, completeForm, false));
    }

    public Map<String, ConfigObj> getVerseMap(){
        return verse;
    }
    

    //    public static void main( String[] args ){
    public String convert(String content, boolean addPause) {

        return convertVerse(content);

    }
    public String convert(String content) {

       return convertVerse(content);

    }

    private static String mapBookAbbre(String book) {

        if (null == book || book.trim().length() <= 0) {
            return book;
        }

        book = book.replace(".", "");

        if (verse.containsKey(book)) {
            return verse.get(book.trim()).getFullWord();
        }

        return book;
    }

    public String appendNextCharTillCompleteVerse(String content, String verse, int start, int end) {

        List<String> verseParts = new ArrayList<>();
        verseParts.addAll(getAllowedPunctuations());

        verseParts.addAll(List.of("上", "下", "章", "篇", "节", "節", "到", "至", "十"));
        for (int i = 0; i < 10; i++) {
            verseParts.add(String.valueOf(i));
            verseParts.add(NumberConverter.toChineseNumber(i));
        }

        StringBuilder builder = new StringBuilder(verse);
        for (int i = start; i < end; i++) {

            String nextChar = content.substring(i, i + 1);
            if (!verseParts.contains(nextChar)) {
                break;
            }
            builder.append(nextChar);
        }

        return builder.toString();
    }

    public Pattern getVersePattern() {
        if (versePattern != null) {
            return versePattern;
        }

        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = ThreadStorage.getVerse().getVerseMap().keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(key.toString()).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append(")\\s{0,}[0-9一二三四五六七八九十百千零]{1,5}[:|：]{0,}\\){0,})");

//        logger.info(builder.toString());

        versePattern = Pattern.compile(builder.toString());
        return versePattern;
    }

    //弗6
    //弗6-7
    //弗6;7
    //弗6,7
    //弗6:13

    public List<String> analyseVerse(String line) {
        Pattern p = getVersePattern();
        Matcher m = p.matcher(line);
        List<String> result = new ArrayList<>();
        if (m.find()) {
            String book = mapBookAbbre(m.group(2));
            String grabbedVerse = appendNextCharTillCompleteVerse(line, m.group(0), m.end(), line.length());
            String verse_str = grabbedVerse.replaceFirst(m.group(2), "");
            result.add(book);
            if( StringUtils.contains(verse_str, ",") || StringUtils.contains(verse_str, ";")
                    || StringUtils.contains(verse_str, "，") || StringUtils.contains(verse_str, "；")  ){
                List<String> list = splitVerseWithCommaAndSemicollon(verse_str);
                result.addAll(list);
            }else{
                result.add(verse_str);
            }

        }

        return result;
    }

    private List<String> splitVerseWithCommaAndSemicollon(String verseStr) {

        List<String> splitters = List.of(",", ";", "，", "；");
        List<String> result = new ArrayList<>();
        for( String splitter : splitters ){
            if( !StringUtils.contains(verseStr, splitter) ){
                continue;
            }
            result.addAll(List.of(StringUtils.split(verseStr, splitter)));
        }

        return result;
    }

    public String convertVerse(String line) {
        Pattern p = getVersePattern();
        Matcher m = p.matcher(line);
        int start = 0;
        String result = line;
        while (m.find(start)) {
            String book = mapBookAbbre(m.group(2));
            String grabbedVerse = appendNextCharTillCompleteVerse(line, m.group(0), m.end(), line.length());
            String verse_str = grabbedVerse.replaceFirst(m.group(2), "");
            String completeVerse = generateCompleteVerses(book, verse_str);

            start = m.end();
//            logger.info("orginal " + grabbedVerse);
//            logger.info("completeVerse " + completeVerse);

            result = result.replaceFirst(grabbedVerse, completeVerse);
        }

        return result;
    }

    //弗6:13-17
    //弗6:13, 17, 19
    //弗6:13-7:4
    //弗6-7:4
    //弗6:13, 14, 17-19
    //弗6:13, 7:14, 17-19
    //弗6:13-17; 14-20
    //弗6:13-17; 7:14-20
    //弗6:13-17; 14, 19, 20
    //弗6:13;7:19-20
    //弗6:13,7:19-20
    protected String generateCompleteVerses(String book, String verse_str) {
        String result = "";
        for (char c : verse_str.toCharArray()) {
            if (c == ':') {
                result += returnChapterWord(book);
                continue;
            }

            if (c == ';') {
                String input = verse_str.substring(verse_str.indexOf(";") + 1);
                if (!StringUtils.isEmpty(input.trim())) {
                    result = book + result + decideChapterVerseWord(book, result) + c + generateCompleteVerses(book, input);
                    return result;
                } else {
                    result += decideChapterVerseWord(book, result) + c;
                    continue;
                }
            }

            if (c == ',') {
                result += decideChapterVerseWord(book, result) + c;
                continue;
            }
            if (containHyphen(String.valueOf(c))) {
                result += "到";
                continue;
            }

            result += c;
        }

        if (stringEndWithVerseNumber(result) &&
                ( (!result.endsWith("節")||!result.endsWith("节")) || !result.endsWith(returnChapterWord(book)))) {
            result += decideChapterVerseWord(book, result);
        }
        if (stringEndWithPunctuationPattern(result) &&
                ( (!result.endsWith("節")||!result.endsWith("节")) || !result.endsWith(returnChapterWord(book)))) {
            result = StringUtils.substring(result, 0, result.length() - 2) + decideChapterVerseWord(book, result) + StringUtils.substring(result, result.length() - 2, result.length() - 1);
        }

        return book + result;
    }

    private String decideChapterVerseWord(String book, String input) {
        String str = StringUtils.reverse(input.replaceAll(" ", ""));
        String chapterWord = returnChapterWord(book);
        boolean hasComma = false;
        boolean hasChapter = false;
        boolean hasVerse = false;
        for (char c : str.toCharArray()) {
            if (c == ',') {
                hasComma = true;
            }

            if (chapterWord.equals(String.valueOf(c))) {
                hasChapter = true;
                break;
            }
            if ("節".equals(String.valueOf(c))||"节".equals(String.valueOf(c))) {
                hasVerse = true;
                break;
            }
        }

        if (hasComma && hasVerse) {
            return "節";
        }
        if (!hasComma && hasChapter) {
            return "節";
        }
        if (!hasComma && !hasChapter && !hasVerse) {
            return chapterWord;
        }
        if (hasComma && hasChapter) {
            return chapterWord;
        }

        return "";
    }

    private boolean stringEndWithVerseNumber(String input) {
        return endWithVerseNumberPattern.matcher(input).find();
    }

    private boolean stringEndWithPunctuationPattern(String input) {
        return endWithPunctuationPattern.matcher(input).find();
    }

    private String returnChapterWord(String book) {
        String chapter = "章";
        if (StringUtils.equalsIgnoreCase("詩篇", book)
                || StringUtils.equalsIgnoreCase("诗篇", book)
                || StringUtils.equalsIgnoreCase("詩", book)
                || StringUtils.equalsIgnoreCase("诗", book)) {
            chapter = "篇";
        }
        return chapter;
    }

}
