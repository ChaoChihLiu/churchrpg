package com.cpbpc.rpgv2;

import com.cpbpc.rpgv2.util.ThreadStorage;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.rpgv2.util.PauseTool.changeFullCharacter;
import static com.cpbpc.rpgv2.util.PauseTool.getPauseTag;
import static com.cpbpc.rpgv2.util.PauseTool.replacePunctuationWithPause;
import static com.cpbpc.rpgv2.util.TextUtil.removeHtmlTag;

public abstract class AbstractArticleParser {

    private static Logger logger = Logger.getLogger(AbstractArticleParser.class.getName());

    protected final String[] hyphens_unicode = new String[]{"\\u002d", "\\u2010", "\\u2011", "\\u2012", "\\u2013", "\\u2015", "\\u2212"};
    protected String content;
    protected String title;

    protected AbbreIntf abbr = ThreadStorage.getAbbreviation();
    protected VerseIntf verse = ThreadStorage.getVerse();
    protected PhoneticIntf phonetic = ThreadStorage.getPhonetics();

    public AbstractArticleParser(String content, String title) {
        this.content = changeFullCharacter(content);
        this.title = changeFullCharacter(title);
    }

    protected String replaceSpace(String input) {
        return RegExUtils.replaceAll(input, "&nbsp;", " ");
    }

    public static void main(String args[]) {
        try {

            String propPath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-chinese.properties";
            FileInputStream in = new FileInputStream(propPath);
            AppProperties.getProperties().load(in);

            try {
                Connection conn = DriverManager.getConnection(AppProperties.getProperties().getProperty("db_url"),
                        AppProperties.getProperties().getProperty("db_username"),
                        AppProperties.getProperties().getProperty("db_password"));
                AbbreIntf abbr = ThreadStorage.getAbbreviation();
                PhoneticIntf phonetic = ThreadStorage.getPhonetics();
                VerseIntf verse = ThreadStorage.getVerse();

                PreparedStatement state = conn.prepareStatement("select * from cpbpc_abbreviation order by seq_no asc, length(short_form) desc");
                ResultSet rs = state.executeQuery();

                while (rs.next()) {
                    String group = rs.getString("group");
                    String shortForm = rs.getString("short_form");
                    String completeForm = rs.getString("complete_form");
                    String isPaused = rs.getString("is_paused");

                    if ("bible".toLowerCase().equals(group.toLowerCase())) {
                        verse.put(shortForm, completeForm, (isPaused.equals("1")) ? true : false);
                    } else if ("pronunciation".toLowerCase().equals(group.toLowerCase())) {
                        phonetic.put(shortForm, completeForm, (isPaused.equals("1")) ? true : false);
                    } else {
                        abbr.put(shortForm, completeForm, (isPaused.equals("1")) ? true : false);
                    }

                }
            } catch (Exception e) {
                logger.info(e.getMessage());
            }

            String content = Files.readString(new File("src/main/resources/chinese-sample.txt").toPath());

            AbstractArticleParser parser = new com.cpbpc.rpgv2.zh.ArticleParser(content, "你们的生命是什么呢？");
            AbstractComposer composer = new com.cpbpc.rpgv2.zh.Composer(parser);

            String script = composer.toPolly();
            System.out.println(script);
//            AWSUtil.putScriptToS3(script, "2023-12-30");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getTitle() {
        return title;
    }

    public abstract List<String> readParagraphs();

    public abstract String readThought();

    public abstract String readPrayer();

    public String readFocusScripture() {
        String content_removed = removeUnwantedBetweenQuotes(content);
        int anchorPoint = StringUtils.indexOf(content_removed, title);
        Pattern pattern = getFocusScripturePattern();
        Matcher m = pattern.matcher(content_removed);
        if (m.find()) {
            String targe = m.group(2);
            int position = m.start();
            if (position > anchorPoint) {
                return "";
            }
            String result = replacePunctuationWithPause(removeHtmlTag(replaceWithPause(replaceSpace(targe))));
            return result;
        }

        return "";
    }
    private final List<Pattern> quote_patterns = List.of(Pattern.compile("(\")(([^\"]|\\\\\")*)(\")"), Pattern.compile("(“)(([^“])*)(”)"));
    private String removeUnwantedBetweenQuotes(String input) {
        String changed = input;

        Map<String, String> replacements = new HashMap<>();
        for (Pattern p : quote_patterns) {

            Matcher matcher = p.matcher(input);
            while (matcher.find()) {
                String orginal = matcher.group(2);
                String replace = orginal.replaceAll("<[^>]*>|&nbsp;|&zwnj;|&raquo;|&laquo;|&gt;|\n", " ");
                replacements.put(orginal, replace);
            }

        }

        Set<Map.Entry<String, String>> entries = replacements.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            changed = changed.replace(entry.getKey(), entry.getValue());
        }

        return changed;
    }
    
    protected String replaceWithPause(String input) {
        if (StringUtils.isEmpty(input)) {
            return "";
        }

        return RegExUtils.replaceAll(input, "<br\\s{0,}/>", getPauseTag(200));
//        return input.replaceAll("<br/>", getPauseTag(200))
//                .replaceAll("<br />", getPauseTag(200));
    }

    protected abstract Pattern getFocusScripturePattern();

    public String readDate() {
        Pattern datePattern = getDatePattern();
        Matcher m = datePattern.matcher(content);
        if (m.find()) {
            String target = m.group();
            return replaceSpace(target);
        }
        return "";
    }

    public List<String> readTopicVerses() {
        List<String> result = new ArrayList<>();
        Pattern versePattern = getTopicVersePattern();
        Matcher m = versePattern.matcher(content);
        int anchorPoint = getAnchorPointAfterScriptureFocus();
        while (m.find()) {
            String target = m.group();
            int position = m.end();
            if (position > anchorPoint) {
                break;
            }
            result.add(replaceSpace(appendNextCharTillCompleteVerse(target, position, anchorPoint)));
        }
        return result;
    }

    protected List<String> getAllowedPunctuations() {
        List<String> punctuations = new ArrayList<>();
        punctuations.addAll(List.of(":", ",", " ", ";", "：", "，", "；"));
        for (String hyphen_unicode : hyphens_unicode) {
            punctuations.add(StringEscapeUtils.unescapeJava(hyphen_unicode));
        }
        return punctuations;
    }

    protected abstract String appendNextCharTillCompleteVerse(String content, String verse, int start, int end) ;
    
    protected String appendNextCharTillCompleteVerse(String verse, int start, int end) {
        return appendNextCharTillCompleteVerse(content, verse, start, end);
    }

    public int getAnchorPointAfterScriptureFocus() {
        String tag = getParagraphTag();
        if (StringUtils.isEmpty(content) || !StringUtils.contains(content, tag)) {
            return 0;
        }
        return StringUtils.indexOf(content, tag, 0);
    }

    protected int getAnchorPointAfterTitle() {
        if (StringUtils.isEmpty(title)) {
            return 0;
        }
        if (StringUtils.isEmpty(content) || !StringUtils.contains(content, title)) {
            return 0;
        }
        return StringUtils.indexOf(content, title, 0) + title.length();
    }

    protected int findNextParagraph(String paragraphTag, int start) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(paragraphTag)) {
            return 0;
        }

        return StringUtils.indexOf(content, paragraphTag, start);
    }
    protected int findLastParagraph(String paragraphTag, int start) {
        if (StringUtils.isEmpty(content) || StringUtils.isEmpty(paragraphTag)) {
            return 0;
        }

        return StringUtils.indexOf(content, "<strong>THOUGHT:</strong>", start);
    }

    protected String getParagraphTag() {
        return "<p>&nbsp;</p>";
    }
    protected String getEndParagraphTag() {
        return "<div style=\"text-align: justify;\"> </div>";
    }

    protected String mapBookAbbre(String book) {

        if (null == book || book.trim().length() <= 0) {
            return book;
        }

        book = book.replace(".", "");

        Map<String, ConfigObj> verseMap = verse.getVerseMap();
        Set<Map.Entry<String, ConfigObj>> entries = verseMap.entrySet();
        for (Map.Entry<String, ConfigObj> entry : entries) {
            ConfigObj obj = entry.getValue();
            if (StringUtils.equalsIgnoreCase(book, obj.getShortForm())) {
                return obj.getFullWord();
            }
        }

        return book;
    }

    protected String getHyphen(String verseStr) {

        for (String hyphen_unicode : hyphens_unicode) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verseStr.contains(hyphen)) {
                return hyphen;
            }
        }

        return "";
    }

    protected boolean containHyphen(String verseStr) {

        for (String hyphen_unicode : hyphens_unicode) {
            String hyphen = StringEscapeUtils.unescapeJava(hyphen_unicode);
            if (verseStr.contains(hyphen)) {
                return true;
            }
        }

        return false;
    }

    protected abstract Pattern getTopicVersePattern();

    protected abstract Pattern getVersePattern();

    protected abstract Pattern getDatePattern();

    protected abstract String generateCompleteVerses(String book, String verse_str);

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
            result.add(StringUtils.trim(book));
            result.add(StringUtils.trim(verse_str));
        }

        return result;
    }
    
    public abstract String convertVerse(String line);

}
