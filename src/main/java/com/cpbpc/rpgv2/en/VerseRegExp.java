package com.cpbpc.rpgv2.en;

import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.ConfigObj;
import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

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
import static com.cpbpc.comms.PunctuationTool.getHyphen;

public class VerseRegExp implements VerseIntf {
    
    private static final Map<String, ConfigObj> verse = new LinkedHashMap<>();
    private static Logger logger = Logger.getLogger(VerseRegExp.class.getName());
    private final Pattern endWithVerseNumberPattern = Pattern.compile("\\d$");
    private final Pattern endWithPunctuationPattern = Pattern.compile("\\d[,|;|\\.]$");

    public void put(String shortForm, String completeForm, boolean isPaused) {
        verse.put(shortForm, new ConfigObj(shortForm, completeForm, false));
    }

    public Map<String, ConfigObj> getVerseMap(){
        return verse;
    }

    //    public static void main( String[] args ){
    public String convert(String content, boolean addPause) {
        return convertVerse(content, addPause);
    }

    @Override
    public String convert(String content, Pattern pattern) {
        return convertVerse(content, false);
    }

    public String convert(String content) {
        return convertVerse(content, false);
    }

    private Pattern versePattern = null;
    public Pattern getVersePattern() {
        if (versePattern != null) {
            return versePattern;
        }

        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = ThreadStorage.getVerse().getVerseMap().keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(key).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append(")[.]{0,1}\\s{0,}[0-9]{1,3})");

//        logger.info(builder.toString());

        versePattern = Pattern.compile(builder.toString());
        return versePattern;
    }

    protected Pattern genSingleVersePattern() {
        String singleVersePattern = "\\(\\s{0,}[vV]{1,2}\\.{0,}\\s{0,}[0-9]{1,3}[,";

        for (String hyphen_unicode : PunctuationTool.getHyphensUnicode()) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            singleVersePattern += hyphen;
        }
        singleVersePattern += "]{0,}\\s{0,}[0-9]{0,3}\\s{0,}\\)";

        return Pattern.compile(singleVersePattern);
    }

    //Eph 6:13-17
    //Eph 6:13, 17, 19
    //Eph 6:13-7:4
    //Eph 6-7:4
    //Eph 6:13, 14, 17-19
    //Eph 6:13, 7:14, 17-19
    //Eph 6:13-17; 14-20
    //Eph 6:13-17; 7:14-20
    //Eph 6:13-17; 14, 19, 20
    //Eph 6:13;7:19-20
    //Eph 6:13,7:19-20
    protected String generateCompleteVerses(String book, String verse_input) {
        String verse_str = StringUtils.trim(verse_input);
        String result = "";
        for (char c : verse_str.toCharArray()) {
            if (c == ':') {
                result = attachChapterWord(book, result);
                result += " verse ";
                continue;
            }

            if (c == ';') {
                String input = verse_str.substring(verse_str.indexOf(";") + 1);
                if (!StringUtils.isEmpty(input.trim())) {
                    result = book + " " + result + c + generateCompleteVerses(book, input);
                    return result;
                }
                result += c;
                continue;
            }

            if (c == ',') {
                result += c;
                continue;
            }
            if (containHyphen(String.valueOf(c))) {
                if(!result.contains(StringUtils.trim(returnChapterWord(book)))) {
                    result = attachChapterWord(book, result);
                }
                result += " to ";
                continue;
            }

            result += c;
        }

        if (stringEndWithVerseNumber(result) && !result.contains(StringUtils.trim(returnChapterWord(book)))){
            result = attachChapterWord(book, result);
        }
        if (stringEndWithPunctuationPattern(result) && !result.contains(StringUtils.trim(returnChapterWord(book)))){
            result = attachChapterWord(book, result);
        }

        return book + " " + result;
    }

    private static Pattern AllChapterPattern = Pattern.compile("^[0-9,\\s]+$");
    private String attachChapterWord(String book, String input) {

        Matcher matcher = AllChapterPattern.matcher(input);
        if( matcher.find() ){
            return returnChapterWord(book) + input;
        }
        String reversed_word = StringUtils.reverse(returnChapterWord(book));
        String trimmed = StringUtils.trim(input);
        String reversed = StringUtils.reverse(trimmed);
        String result = "";
        boolean isAttached = false;
        for( char c : reversed.toCharArray() ){
            if( !isAttached && !NumberUtils.isCreatable(String.valueOf(c)) ){
                result += reversed_word;
                isAttached = true;
            }
            result += c;
        }

        return StringUtils.reverse(result);
    }

    private boolean stringEndWithVerseNumber(String input) {
        return endWithVerseNumberPattern.matcher(input).find();
    }

    private boolean stringEndWithPunctuationPattern(String input) {
        return endWithPunctuationPattern.matcher(input).find();
    }

    public String appendNextCharTillCompleteVerse(String content, String verse, int start, int end) {
        List<String> verseParts = new ArrayList<>();
        verseParts.addAll(getAllowedPunctuations());

        for (int i = 0; i < 10; i++) {
            verseParts.add(String.valueOf(i));
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
    
    private String returnChapterWord(String book) {
        String chapter = " chapter ";
        List<String> psalms = new ArrayList<>();
        psalms.addAll(List.of("Psalms", "Ps", "Psalm", "Pslm", "Psa", "Psm", "Pss"));
        if ( psalms.contains(book) ) {
            chapter = " ";
        }
        return chapter;
    }

    public String convertVerse(String line, boolean addPause) {
        return convertSingleVerse(convertNormalVerse(line, addPause));
    }

    private String convertSingleVerse(String line) {
        Pattern p = genSingleVersePattern();
        Matcher m = p.matcher(line);
        int start = 0;
        String result = line;
        while (m.find(start)) {
            String target = StringUtils.lowerCase(StringUtils.trim(m.group(0)));
            String replacement = target;
            if( target.contains("vv") ){
                replacement = RegExUtils.replaceFirst(replacement, "vv", "verses");
            }
            if( target.contains("v") && !target.contains("vv") ){
                replacement = RegExUtils.replaceFirst(replacement, "v", "verse");
            }
            if( containHyphen(target) ){
                String hyphen = getHyphen(target);
                replacement = RegExUtils.replaceFirst(replacement, hyphen, " to ");
            }
            replacement = RegExUtils.replaceFirst(replacement, "\\.", "");

            start = m.end();
            result = StringUtils.replace(result, target, replacement);
        }

        return result;
    }
    
    private String convertNormalVerse(String line, boolean addPause) {
        Pattern p = getVersePattern();
        Matcher m = p.matcher(line);
        int start = 0;
        String result = line;
        while (m.find(start)) {
            String group0 = m.group(0);
            String book_str = m.group(2);
            int matched_end = m.end();
            start = matched_end;
            String grabbedVerse = appendNextCharTillCompleteVerse(line, group0, matched_end, line.length());
            String verse_str = grabbedVerse.replaceFirst(book_str, "");
            String book = makeItPlural(mapBookAbbre(book_str), verse_str);
            String completeVerse = generateCompleteVerses(book, verse_str);
            completeVerse = StringUtils.replaceAll(completeVerse, "\\.", "");


//            logger.info("orginal " + grabbedVerse);
//            logger.info("completeVerse " + completeVerse);

            result = result.replaceFirst(grabbedVerse, completeVerse);
            if( addPause ){
                result += PunctuationTool.getPauseTag(400);
            }
        }

        return result;
    }

    private String makeItPlural(String book, String verseStr) {
        if( !StringUtils.equalsIgnoreCase(book, "Psalms") && !StringUtils.equalsIgnoreCase(book, "Psalm") ){
            return book;
        }

        if( StringUtils.countMatches(verseStr, ":") >=2 ){
            return "Psalms";
        }
        String hyphen = getHyphen(verseStr);
        if( !StringUtils.isEmpty(hyphen)
                && StringUtils.countMatches(verseStr, hyphen) >= 1
                && StringUtils.countMatches(verseStr, ":") <= 0 ){
            return "Psalms";
        }

        return "Psalm";
    }

    private  String mapBookAbbre(String book) {

        if (null == book || book.trim().length() <= 0) {
            return book;
        }

        book = book.replace(".", "").replace("&nbsp;", " ");

        if (verse.containsKey(book.trim())) {
            return verse.get(book.trim()).getFullWord();
        }

        return book;
    }

    //弗6
    //弗6-7
    //弗6;7
    //弗6,7
    //弗6:13

    public List<String> analyseVerse(String line) {
        Pattern p = getVersePattern();
        return analyseVerse(line, p);
    }

    @Override
    public List<String> analyseVerse(String line, Pattern pattern) {
        Matcher m = pattern.matcher(line);
        List<String> result = new ArrayList<>();
        if (m.find()) {
            String group0 = m.group(0);
            String book_str = m.group(2);
            int matched_end = m.end();

            String book = mapBookAbbre(book_str);
            String grabbedVerse = appendNextCharTillCompleteVerse(line, group0, matched_end, line.length());
            String verse_str = grabbedVerse.replaceFirst(book_str, "");
            result.add(StringUtils.trim(book));
            result.add(StringUtils.trim(verse_str));
        }

        return result;
    }

}
