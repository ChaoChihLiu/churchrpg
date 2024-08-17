package com.cpbpc.rpgv2.zh;

import com.cpbpc.comms.AbstractArticleParser;
import com.cpbpc.comms.Article;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.removeDoubleQuote;
import static com.cpbpc.comms.TextUtil.removeHtmlTag;
import static com.cpbpc.comms.TextUtil.replaceHtmlSpace;

public class ArticleParser extends AbstractArticleParser {
    private static Logger logger = Logger.getLogger(ArticleParser.class.getName());
    
    private static VerseIntf verse = ThreadStorage.getVerse();

    public ArticleParser(Article article) {
        super(article);
    }

    @Override
    public List<String> readParagraphs() {

        VerseIntf verse = ThreadStorage.getVerse();
        int titlePosition = getAnchorPointAfterTitle(title, content);
        int nextParaPosistion = findNextParagraph(getParagraphTag(), titlePosition);
        String text = StringUtils.substring(content, titlePosition, nextParaPosistion);

        List<String> result = new ArrayList<>();
        List<String> splits = List.of(text.split("</p>"));
        try{
            for (String split : splits) {
                if( StringUtils.contains(split, thoughtWord1)
                        || StringUtils.contains(split, prayerWord1)
                        || StringUtils.contains(split, thoughtWord2)
                        || StringUtils.contains(split, prayerWord2)){
                    continue;
                }
                String line = StringUtils.trim(removeHtmlTag(split));
                line = removeDoubleQuote(line);
                if (!StringUtils.isEmpty(line)) {
                    result.add(verse.convert(replaceHtmlSpace(line)));
                }
            }
        }catch (Exception e){
            logger.info(ExceptionUtils.getStackTrace(e));
        }

        return result;
    }

    private static String thoughtWord1 = "<strong>默想:</strong>";
    private static String thoughtWord2 = "<strong>默想</strong>:";
    @Override
    public String readThought() {
        String[] lines = StringUtils.split(content, System.lineSeparator());
        String result = "";
        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            line = removeDoubleQuote(line);
            if (StringUtils.contains(line, thoughtWord1)) {
                result = removeHtmlTag(line).trim();
            }
            if (StringUtils.contains(line, thoughtWord2)) {
                result = removeHtmlTag(line).trim();
            }
        }

        return verse.convert(replaceHtmlSpace(result));
    }

    private static String prayerWord1 = "<strong>祷告:</strong>";
    private static String prayerWord2 = "<strong>祷告</strong>:";
    @Override
    public String readPrayer() {
        String[] lines = StringUtils.split(content, System.lineSeparator());
        String result = "";
        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            line = removeDoubleQuote(line);
            if (StringUtils.contains(line, prayerWord1)) {
                result = removeHtmlTag(line).trim();
            }
            if (StringUtils.contains(line, prayerWord2)) {
                result = removeHtmlTag(line).trim();
            }
        }

        return replaceHtmlSpace(verse.convert(result));
    }

    @Override
    public String readEnd() {
        return "";
    }

    @Override
    protected Pattern getFocusScripturePattern() {
        return Pattern.compile("(“)(.*)(”)");
    }

    @Override
    protected Pattern getDatePattern() {
        return Pattern.compile("[一二三四五六七八九十百千零月日,]{4,9}");
    }

    @Override
    public Pattern getTopicVersePattern() {
        VerseIntf verse = ThreadStorage.getVerse();
        StringBuilder builder = new StringBuilder("((");

        Set<String> keySet = verse.getVerseMap().keySet();
        for (String key : keySet) {
            builder.append(key.toString()).append("|")
                    .append(key.toString()).append("&nbsp;|")
                    .append(key.replace(" ", "&nbsp;")).append("|")
            ;
        }
        if (builder.toString().endsWith("|")) {
            builder.delete(builder.length() - 1, builder.length());
        }
        builder.append(")\\s{0,}[0-9一二三四五六七八九十百千零至到]{1,}\\s{0,}[章|篇])");

        return Pattern.compile(builder.toString());

    }
    
}
