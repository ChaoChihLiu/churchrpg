package com.cpbpc.rpgv2;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.ThreadStorage;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import static com.cpbpc.comms.PunctuationTool.changeFullCharacter;
import static com.cpbpc.comms.PunctuationTool.getPauseTag;
import static com.cpbpc.comms.TextUtil.removeHtmlTag;

public abstract class AbstractArticleParser {

    private static Logger logger = Logger.getLogger(AbstractArticleParser.class.getName());

    protected String content;
    protected String title;

    protected AbbreIntf abbr = ThreadStorage.getAbbreviation();
    protected VerseIntf verse = ThreadStorage.getVerse();

    public AbstractArticleParser(String content, String title) {
        this.content = changeFullCharacter(ZhConverterUtil.toSimple(content));
        this.title = changeFullCharacter(ZhConverterUtil.toSimple(title));
    }

    protected String replaceSpace(String input) {
        return RegExUtils.replaceAll(input, "&nbsp;", " ");
    }

    public static void main(String args[]) {
        try {
            String language = "chinese";

            String propPath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-"+language+".properties";
            FileInputStream in = new FileInputStream(propPath);
            AppProperties.getConfig().load(in);

            try {
                Connection conn = DriverManager.getConnection(AppProperties.getConfig().getProperty("db_url"),
                        AppProperties.getConfig().getProperty("db_username"),
                        AppProperties.getConfig().getProperty("db_password"));
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

            String content = Files.readString(new File("src/main/resources/"+language+"-sample.txt").toPath());

            AbstractArticleParser parser = null;
            AbstractComposer composer = null;
            if( language.equals("chinese") ){
                parser = new com.cpbpc.rpgv2.zh.ArticleParser(content, "耶弗他（三）");
                composer = new com.cpbpc.rpgv2.zh.Composer(parser);
            } else{
                parser = new com.cpbpc.rpgv2.en.ArticleParser(content, "GOD’S WORD FOR STRANGERS");
                composer = new com.cpbpc.rpgv2.en.Composer(parser);
            }

            List<ComposerResult> results = composer.toPolly(true, "2024-02-04");
            StringBuilder script = new StringBuilder();
            for(ComposerResult result : results){
                script.append(result.getScript());
            }
            IOUtils.write(script, new FileOutputStream(new File("script.txt")));
            IOUtils.write(removeHtmlTag(script.toString()), new FileOutputStream(new File("script-no-tag.txt")));
            System.out.println(script);
//            OpenAIUtil.textToSpeech(removeHtmlTag("Girgashites"), "echo");
//            AWSUtil.putScriptToS3(script, "2024-01-07");

        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
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
            String result = removeHtmlTag(replaceWithPause(replaceSpace(targe)));
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
                String replace = orginal.replaceAll("<[^>]*>|&nbsp;|&zwnj;|&raquo;|&laquo;|&gt;|\n|\r\n", " ");
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
        VerseIntf verse = ThreadStorage.getVerse();
        List<String> result = new ArrayList<>();
        Pattern versePattern = getTopicVersePattern();
        Matcher m = versePattern.matcher(content);
        int anchorPoint = getAnchorPointAfterScriptureFocus();
        int start = 0;
        while (m.find(start)) {
            String target = m.group();
            int position = m.end();
            start = position;
            if (position > anchorPoint) {
                break;
            }
            result.add(replaceSpace(verse.appendNextCharTillCompleteVerse(content, target, position, anchorPoint)));
        }
        return result;
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


    public abstract Pattern getTopicVersePattern();

    protected abstract Pattern getDatePattern();
    
}
