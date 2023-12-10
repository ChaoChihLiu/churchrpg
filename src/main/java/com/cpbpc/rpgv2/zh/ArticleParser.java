package com.cpbpc.rpgv2.zh;

import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.AbstractArticleParser;
import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.removeDoubleQuote;
import static com.cpbpc.comms.PunctuationTool.replacePunctuationWithPause;
import static com.cpbpc.comms.TextUtil.removeHtmlTag;

public class ArticleParser extends AbstractArticleParser {
    private static Logger logger = Logger.getLogger(ArticleParser.class.getName());
    
    private static VerseIntf verse = ThreadStorage.getVerse();

    public ArticleParser(String content, String title) {
        super(content, title);
    }

    @Override
    public List<String> readParagraphs() {

        VerseIntf verse = ThreadStorage.getVerse();
        int titlePosition = getAnchorPointAfterTitle();
        int nextParaPosistion = findNextParagraph(getParagraphTag(), titlePosition);
        String text = StringUtils.substring(content, titlePosition, nextParaPosistion);

        List<String> result = new ArrayList<>();
        List<String> splits = List.of(text.split("</p>"));
        try{
            for (String split : splits) {
                String line = StringUtils.trim(removeHtmlTag(split));
                line = removeDoubleQuote(line);
                if (!StringUtils.isEmpty(line)) {
                    result.add(verse.convert(replaceSpace(line)));
                }
            }
        }catch (Exception e){
            logger.info(ExceptionUtils.getStackTrace(e));
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
            line = removeDoubleQuote(line);
            if (StringUtils.contains(line, "<strong>默想</strong>")) {
                result = removeHtmlTag(line).trim();
            }
        }

        return replacePunctuationWithPause(verse.convert(replaceSpace(result)));
    }

    @Override
    public String readPrayer() {
        String[] lines = StringUtils.split(content, System.lineSeparator());
        String result = "";
        for (String line : lines) {
            if (StringUtils.isEmpty(line)) {
                continue;
            }
            line = removeDoubleQuote(line);
            if (StringUtils.contains(line, "<strong>祷告</strong>")) {
                result = removeHtmlTag(line).trim();
            }
        }

        return replacePunctuationWithPause(replaceSpace(verse.convert(result)));
    }

    @Override
    protected Pattern getFocusScripturePattern() {
        return Pattern.compile("(“)(.*)(”)");
    }

    @Override
    protected Pattern getDatePattern() {
        return Pattern.compile("[一二三四五六七八九十百千零月日主礼拜,]{7,11}");
    }

    @Override
    protected Pattern getTopicVersePattern() {
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
        builder.append(")\\s{0,}[0-9一二三四五六七八九十百千零]{1,}\\s{0,}[章|篇])");

        return Pattern.compile(builder.toString());

    }
    
}
