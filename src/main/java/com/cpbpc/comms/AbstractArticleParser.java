package com.cpbpc.comms;

import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
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
import static com.cpbpc.comms.TextUtil.replaceHtmlSpace;

public abstract class AbstractArticleParser {

    private static Logger logger = Logger.getLogger(AbstractArticleParser.class.getName());

    protected String content;
    protected String title;
    protected Article article;
    
    protected VerseIntf verse = ThreadStorage.getVerse();

    public AbstractArticleParser(Article article) {
        this.article = article;
        this.content = changeFullCharacter(ZhConverterUtil.toSimple(article.getContent()));
        this.content = this.content.replaceAll("\\.\\.\\.", ".").replaceAll("\\.\\.", ".");
        this.title = changeFullCharacter(ZhConverterUtil.toSimple(article.getTitle()));
        if( AppProperties.isChinese() ){
            this.title = StringUtils.remove(this.title, " ");
        }
    }

    public static void main(String args[]) {
        try {
            String language = "chinese";

            String propPath = "/Users/liuchaochih/Documents/GitHub/churchrpg/src/main/resources/app-"+language+".properties";
            FileInputStream in = new FileInputStream(propPath);
            AppProperties.getConfig().load(in);

            try {
//                Connection conn = DriverManager.getConnection(AppProperties.getConfig().getProperty("db_url"),
//                        AppProperties.getConfig().getProperty("db_username"),
//                        AppProperties.getConfig().getProperty("db_password"));
                DBUtil.initStorage(AppProperties.getConfig());
            } catch (Exception e) {
                logger.info(e.getMessage());
            }

            String content = Files.readString(new File("src/main/resources/"+language+"-sample.txt").toPath());

            AbstractArticleParser parser = null;
            AbstractComposer composer = null;
            if( language.equals("chinese") ){
                parser = new com.cpbpc.rpgv2.zh.ArticleParser(new Article("2024-11-06", content, "法律书已全部阅读完毕", "", 1));
                composer = new com.cpbpc.rpgv2.zh.Composer(parser);
            } else{
                parser = new com.cpbpc.rpgv2.en.ArticleParser(new Article("2024-10-03", content,  "LIGHT SHINES BRIGHTEST IN DARKEST NIGHT", "", 1));
                composer = new com.cpbpc.rpgv2.en.Composer(parser);
            }

            List<ComposerResult> results = composer.toTTS(true, "2024-09-19");
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
    
    private Pattern buildTitlePattern(String title) {

        StringBuilder builder = new StringBuilder("<strong>\\s*");
        for( char c : title.toCharArray() ){

            if( c == '(' || c == ')' || c == '?' ){
                builder.append('\\');
            }

            builder.append(c);
            if( StringUtils.indexOf(title, c) == title.length()-1 ){
                break;
            }
            if( c == ' ' ){
//                builder.append("[<\\/strong>|<br\\s{0,}/>|<strong>|</p>|<p[^>]*>]{0,}");
                builder.append("(?:<\\/strong>|<br\\s*\\/?>|<strong>|<\\/p>|<p[^>]*>)*\\s*");
            }
        }

        builder.append("\\s*<\\/strong>");

        return Pattern.compile(builder.toString());
    }

    public Article getArticle() {
        return article;
    }

    public abstract List<String> readParagraphs();

    public abstract String readThought();

    public abstract String readPrayer();
    public abstract String readEnd();

    public String readFocusScripture() {
        VerseIntf verseIntf = ThreadStorage.getVerse();
        String content_removed = removeUnwantedBetweenQuotes(content);
        int anchorPoint = getAnchorPointAfterTitle(title, content_removed);
        Pattern pattern = getFocusScripturePattern();
        Matcher m = pattern.matcher(content_removed);
        if (m.find()) {
            String targe = m.group(2);
            int position = m.start();
            if (position > anchorPoint) {
                return "";
            }
            String result = removeHtmlTag(replaceWithPause(replaceHtmlSpace(targe)));
            return verseIntf.convert(result);
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
            return replaceHtmlSpace(target);
        }
        return "";
    }

    public List<String> readTopicVerses(String input) {
        VerseIntf verseIntf = ThreadStorage.getVerse();
        List<String> result = new ArrayList<>();
        Pattern versePattern = getTopicVersePattern();
        Matcher m = versePattern.matcher(input);
        int anchorPoint = getAnchorPointAfterTitle(title, input);
        List<Integer> positions = new ArrayList<>();
        while (m.find()) {
            int pos = m.start();
            if( pos >= anchorPoint ){
                break;
            }
            positions.add(pos);
        }

        if( positions.isEmpty() ){
            return result;
        }
        for( int i = 0; i<positions.size(); i++ ){
            int from = positions.get(i);
            int to = input.length();
            if( positions.size() > (i+1) ){
                to = positions.get(i+1);
            }

            String toProcessed = StringUtils.substring(input, from, to);
            Matcher matcher = versePattern.matcher(toProcessed);
            if (matcher.find()) {
                String group0 = matcher.group(0);
                String book = matcher.group(2);
                
                String grabbedVerse = verseIntf.appendNextCharTillCompleteVerse(toProcessed, group0, group0.length(), toProcessed.length());
                if( StringUtils.contains(grabbedVerse, ";") ){
                    String[] splitted = StringUtils.split(grabbedVerse, ";");
                    for( String element : splitted ){
                        if( StringUtils.startsWith(element, book) ){
                            result.add(element);
                            continue;
                        }
                        result.add(book+" "+element);
                    }
                    continue;
                }
                result.add(grabbedVerse);
            }
        }
        return result;
    }

    public List<String> readTopicVerses() {
        return readTopicVerses(content);
    }

//    public int getAnchorPointAfterScriptureFocus() {
////        String tag = getParagraphTag();
////        if (StringUtils.isEmpty(content) || !StringUtils.contains(content, tag)) {
////            return 0;
////        }
//        return StringUtils.indexOf(content, title, 0)+title.length()+1;
//    }

    protected int getAnchorPointAfterTitle(String title, String content) {
        if (StringUtils.isEmpty(title) || StringUtils.isEmpty(content)) {
            return 0;
        }
//        if (StringUtils.isEmpty(content) || !StringUtils.contains(content, title)) {
//            return 0;
//        }
//        String titleWithTag = "<strong>"+title+"</strong>";
//        return StringUtils.indexOf(content, titleWithTag, 0) + titleWithTag.length();
        Pattern titlePattern = buildTitlePattern( title );
        Matcher matcher = titlePattern.matcher(content);
        while( matcher.find() ){
            String result = matcher.group();
            return StringUtils.indexOf(content, result) + result.length();
        }

        return 0;
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

        if( StringUtils.contains(content, "<strong>THOUGHT:</strong>") ){
            return StringUtils.indexOf(content, "<strong>THOUGHT:</strong>", start);
        }

        if( StringUtils.contains(content, "<strong>MEMORISATION:</strong>") ){
            return StringUtils.indexOf(content, "<strong>MEMORISATION:</strong>", start);
        }

        return -1;
    }

    protected String getParagraphTag() {
        return "<p>&nbsp;</p>";
    }
    protected String getEndParagraphTag() {
        return "<div style=\"text-align: justify;\"> </div>";
    }


    public abstract Pattern getTopicVersePattern();

    protected abstract Pattern getDatePattern();

    public String getTopic(){
        return "";
    }
}
