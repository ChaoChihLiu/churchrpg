package com.cpbpc.rpgv2.zh;

import com.cpbpc.comms.NumberConverter;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.ConfigObj;
import com.cpbpc.rpgv2.VerseIntf;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.containHyphen;
import static com.cpbpc.comms.PunctuationTool.getAllowedPunctuations;
import static com.cpbpc.comms.TextUtil.returnChapterWord;

public class VerseRegExp implements VerseIntf {

    private static Logger logger = Logger.getLogger(VerseRegExp.class.getName());
    private final Pattern endWithVerseNumberPattern = Pattern.compile("\\d$");
    private final Pattern endWithPunctuationPattern = Pattern.compile("\\d[,|;|\\.]$");

    private Pattern versePattern = null;

    private static final Map<String, ConfigObj> verse = new LinkedHashMap<>();
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

        return StringUtils.trim(builder.toString());
    }

    private String buildBibleRefReg(){
        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = ThreadStorage.getVerse().getVerseMap().keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(ZhConverterUtil.toTraditional(key.toString())).append("|")
                    .append(key.toString()).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append(")\\s{0,}[0-9一二三四五六七八九十百千零]{1,5}[:|：]{0,})");

        return builder.toString();
    }

    public Pattern getVersePattern() {
        if (versePattern != null) {
            return versePattern;
        }

        String pattern = buildBibleRefReg();

//        logger.info(builder.toString());

        versePattern = Pattern.compile(pattern);
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
            String group0 = m.group(0);
            String book_simplied = m.group(2);
            int matched_end = m.end();
            String book = mapBookAbbre(book_simplied);
            String grabbedVerse = appendNextCharTillCompleteVerse(line, group0, matched_end, line.length());
            String verse_str = grabbedVerse.replaceFirst(book_simplied, "");
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
            String group0 = m.group(0);
            String book_simplied = m.group(2);
            Integer matched_end = m.end();
            start = matched_end;
            String book = mapBookAbbre(book_simplied);
            String grabbedVerse = appendNextCharTillCompleteVerse(line, group0, matched_end, line.length());
            String verse_str = grabbedVerse.replaceFirst(book_simplied, "");
            String completeVerse = generateCompleteVerses(book, verse_str);
            completeVerse = fix1ChapterBook(book, completeVerse);

//            logger.info("orginal " + grabbedVerse);
//            logger.info("completeVerse " + completeVerse);

            result = result.replaceFirst(grabbedVerse, completeVerse);
        }

        result = convertReference(result);

        return result;
    }

    private String convertReference(String line) {
        Pattern p = getReferencePattern();
        Matcher matcher = p.matcher(line);

        String result = line;
        int start = 0;
        while(matcher.find(start)){
            String found = matcher.group();
            String refWord = matcher.group(2);
            System.out.println(matcher.group(2));
            start = matcher.end();
            int matched_end = start;

            String book = StringUtils.remove(StringUtils.remove(refWord, "参"), ZhConverterUtil.toTraditional("参"));
            if( StringUtils.isEmpty(book) ){
                book = findLastMentionedBook(line, matched_end);
            }

            String grabbedVerse = appendNextCharTillCompleteVerse(line, found, matched_end, line.length());
            String verse_str = grabbedVerse.replaceFirst(refWord, "");
            String completeVerse = generateCompleteVerses(book, verse_str);
            completeVerse = fix1ChapterBook(book, completeVerse);
            result = result.replaceFirst(grabbedVerse, "參考"+completeVerse);
        }

        return result;
    }

    private String findLastMentionedBook(String line, int anchorPoint) {
        Pattern p = getVersePattern();
        Matcher m = p.matcher(line);
        int start = 0;
        String result = "";
        while (m.find(start)) {
            String book_simplied = m.group(2);
            Integer matched_end = m.end();
            start = matched_end;

            if( matched_end >= anchorPoint ){
                return result;
            }

            result = book_simplied;
        }
        
        return result;
    }

    //参1:13-15
    //參1:13-15
    //參士1:13-15
    //參士师记1:13-15
    private Pattern getReferencePattern() {

        StringBuilder builder = new StringBuilder("(([參|参]{1,1}\\s{0,}[");

        Set<String> keySet = ThreadStorage.getVerse().getVerseMap().keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(ZhConverterUtil.toTraditional(key.toString())).append("|")
                    .append(key.toString()).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append("]{0,})\\s{0,}[0-9一二三四五六七八九十百千零]{1,5}[:|：]{0,})");

        return Pattern.compile(builder.toString());
    }

    private static List<String> oneChapterBook = List.of("俄", "俄巴底亞書", "門", "腓利門書", "约二", "約翰二書", "约三", "約翰三書", "猶", "猶大書");

    private String fix1ChapterBook(String book, String completeVerse) {
        String chapterWord = returnChapterWord(book);

        boolean isMatched = false;
        for( String bookName: oneChapterBook ){
            if( !StringUtils.equals(ZhConverterUtil.toSimple(book), ZhConverterUtil.toSimple(bookName)) ){
                continue;
            }
            isMatched = true;
            break;
        }

        if( !isMatched ){
            return completeVerse;
        }

        if( StringUtils.contains(completeVerse, chapterWord)
                && !StringUtils.contains(ZhConverterUtil.toSimple(completeVerse), ZhConverterUtil.toSimple("節")) ){
            return completeVerse.replaceAll(chapterWord, ZhConverterUtil.toSimple("節"));
        }
        return completeVerse;
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


}
