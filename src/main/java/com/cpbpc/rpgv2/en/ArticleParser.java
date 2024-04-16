package com.cpbpc.rpgv2.en;

import com.cpbpc.comms.AbstractArticleParser;
import com.cpbpc.comms.Article;
import com.cpbpc.comms.RomanNumeral;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.pause;
import static com.cpbpc.comms.PunctuationTool.removeDoubleQuote;
import static com.cpbpc.comms.TextUtil.removeHtmlTag;
import static com.cpbpc.comms.TextUtil.replaceHtmlSpace;

public class ArticleParser extends AbstractArticleParser {
    private static Logger logger = Logger.getLogger(ArticleParser.class.getName());
    public ArticleParser(Article article) {
        super(article);
    }
    private VerseIntf verseIntf = ThreadStorage.getVerse();

    @Override
    public List<String> readParagraphs() {

        int titlePosition = getAnchorPointAfterTitle(title, content);
        int nextParaPosistion = findLastParagraph(getEndParagraphTag(), titlePosition);
        String text = StringUtils.substring(content, titlePosition, nextParaPosistion);

        List<String> result = new ArrayList<>();
        List<String> splits = List.of(text.split("<div style=\"text-align: justify;\">[\\s|&nbsp;]{1,}</div>"));
        try{
            for (String split : splits) {
                String line = StringUtils.trim(removeHtmlTag(split));
                line = removeDoubleQuote(line);
                if (!StringUtils.isEmpty(line)) {
                    result.add(RomanNumeral.convert(verseIntf.convert(replaceHtmlSpace(line)), false));
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
                result = replaceHtmlSpace(verseIntf.convert(result));
            }

            if (StringUtils.contains(line, "<strong>MEMORISATION:</strong>")) {
                result = removeHtmlTag(line).trim();
                String ref = result.replace("MEMORISATION:", "").trim();
                List<String> refs = verseIntf.analyseVerse(ref);
                try {
                    String verse = BibleVerseGrab.grab(refs.get(0), refs.get(1));
                    result = replaceHtmlSpace(verseIntf.convert(result)) + pause(400) + verse;
                } catch (IOException e) {
                    logger.info(ExceptionUtils.getStackTrace(e));
                }
            }
        }

        return result;
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

        return replaceHtmlSpace(verseIntf.convert(result));
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
        return Pattern.compile("[A-Z,\\s’]{12,22}\\d{1,2}\\s{0,}[MORNING|EVENING]{0,}");
    }

    @Override
    public Pattern getTopicVersePattern() {
        return verseIntf.getVersePattern();
    }
    
}
