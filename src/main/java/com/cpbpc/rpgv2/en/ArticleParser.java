package com.cpbpc.rpgv2.en;

import com.cpbpc.rpgv2.AbstractArticleParser;
import com.cpbpc.rpgv2.util.RomanNumeral;
import com.cpbpc.rpgv2.util.ThreadStorage;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.rpgv2.util.PauseTool.replacePunctuationWithPause;
import static com.cpbpc.rpgv2.util.TextUtil.removeHtmlTag;

public class ArticleParser extends AbstractArticleParser {
    private static Logger logger = Logger.getLogger(ArticleParser.class.getName());
    private final Pattern endWithVerseNumberPattern = Pattern.compile("\\d$");
    private final Pattern endWithPunctuationPattern = Pattern.compile("\\d[,|;|\\.]$");
    private Pattern versePattern = null;

    public ArticleParser(String content, String title) {
        super(content, title);
    }

    @Override
    public List<String> readParagraphs() {

        int titlePosition = getAnchorPointAfterTitle();
        int nextParaPosistion = findLastParagraph(getEndParagraphTag(), titlePosition);
        String text = StringUtils.substring(content, titlePosition, nextParaPosistion);

        List<String> result = new ArrayList<>();
        List<String> splits = List.of(text.split("<div style=\"text-align: justify;\"> </div>"));
        for (String split : splits) {
            String line = StringUtils.trim(removeHtmlTag(split));
            if (!StringUtils.isEmpty(line)) {
                result.add(RomanNumeral.convert(abbr.convert(convertVerse(replaceSpace(line)))));
            }
        }

        return result;
    }

    @Override
    public String readThought() {
        String[] lines = StringUtils.split(content, System.lineSeparator());
        String result = "";
        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            if (StringUtils.contains(line, "<strong>THOUGHT:</strong>")) {
                result = removeHtmlTag(line).trim();
            }
        }

        return replacePunctuationWithPause(abbr.convert(replaceSpace(convertVerse(result))));
    }

    @Override
    public String readPrayer() {
        String[] lines = StringUtils.split(content, System.lineSeparator());
        String result = "";
        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            if (StringUtils.contains(line, "<strong>PRAYER:</strong>")) {
                result = removeHtmlTag(line).trim();
            }
        }

        return replacePunctuationWithPause(abbr.convert(replaceSpace(convertVerse(result))));
    }

    @Override
    protected Pattern getFocusScripturePattern() {
        return Pattern.compile("(“)(.*)(”)");
    }
    
    @Override
    protected Pattern getDatePattern() {
        return Pattern.compile("[A-Z,\\s’]{12,22}\\d{1,2}");
    }

    @Override
    protected Pattern getTopicVersePattern() {
        return getVersePattern();
    }

    @Override
    protected Pattern getVersePattern() {
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

        logger.info(builder.toString());

        versePattern = Pattern.compile(builder.toString());
        return versePattern;
    }

    protected Pattern genSingleVersePattern() {
        String singleVersePattern = "\\(\\s{0,}[vV]{1,2}\\.{0,}\\s{0,}[0-9]{1,3}[,";

        for (String hyphen_unicode : hyphens_unicode) {
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
    
    protected String appendNextCharTillCompleteVerse(String content, String verse, int start, int end) {
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

    protected String appendNextCharTillCompleteVerse(String verse, int start, int end) {
        return appendNextCharTillCompleteVerse(content, verse, start, end);
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
    
    public String convertVerse(String line) {
        return convertSingleVerse(convertNormalVerse(line));
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

    private String convertNormalVerse(String line) {
        Pattern p = getVersePattern();
        Matcher m = p.matcher(line);
        int start = 0;
        String result = line;
        while (m.find(start)) {
            String book = mapBookAbbre(m.group(2));
            String grabbedVerse = appendNextCharTillCompleteVerse(line, m.group(0), m.end(), line.length());
            String verse_str = grabbedVerse.replaceFirst(m.group(2), "");
            String completeVerse = generateCompleteVerses(book, verse_str);
            completeVerse = StringUtils.replaceAll(completeVerse, "\\.", "");

            start = m.end();
            logger.info("orginal " + grabbedVerse);
            logger.info("completeVerse " + completeVerse);

            result = result.replaceFirst(grabbedVerse, completeVerse);
        }

        return result;
    }
}
