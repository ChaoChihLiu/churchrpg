package com.cpbpc.rpgv2.en;

import com.cpbpc.comms.RomanNumeral;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.AbstractArticleParser;
import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.removeDoubleQuote;
import static com.cpbpc.comms.TextUtil.removeHtmlTag;

public class ArticleParser extends AbstractArticleParser {
    private static Logger logger = Logger.getLogger(ArticleParser.class.getName());
    public ArticleParser(String content, String title) {
        super(content, title);
    }
    private VerseIntf verse = ThreadStorage.getVerse();

    @Override
    public List<String> readParagraphs() {

        int titlePosition = getAnchorPointAfterTitle();
        int nextParaPosistion = findLastParagraph(getEndParagraphTag(), titlePosition);
        String text = StringUtils.substring(content, titlePosition, nextParaPosistion);

        List<String> result = new ArrayList<>();
        List<String> splits = List.of(text.split("<div style=\"text-align: justify;\"> </div>"));
        try{
            for (String split : splits) {
                String line = StringUtils.trim(removeHtmlTag(split));
                line = removeDoubleQuote(line);
                if (!StringUtils.isEmpty(line)) {
                    result.add(RomanNumeral.convert(abbr.convert(verse.convert(replaceSpace(line)))));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
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
            if (StringUtils.contains(line, "<strong>THOUGHT:</strong>")) {
                result = removeHtmlTag(line).trim();
            }
        }

        return abbr.convert(replaceSpace(verse.convert(result)));
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
            if (StringUtils.contains(line, "<strong>PRAYER:</strong>")) {
                result = removeHtmlTag(line).trim();
            }
        }

        return abbr.convert(replaceSpace(verse.convert(result)));
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
    public Pattern getTopicVersePattern() {
        return verse.getVersePattern();
    }
    
}
