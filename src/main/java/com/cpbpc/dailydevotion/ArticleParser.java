package com.cpbpc.dailydevotion;

import com.cpbpc.comms.AbstractArticleParser;
import com.cpbpc.comms.Article;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.PunctuationTool.pause;
import static com.cpbpc.comms.TextUtil.removeDoubleQuote;
import static com.cpbpc.comms.TextUtil.removeHtmlTag;
import static com.cpbpc.comms.TextUtil.removeLinkBreak;
import static com.cpbpc.comms.TextUtil.replaceHtmlSpace;

public class ArticleParser extends AbstractArticleParser {
    private static Logger logger = Logger.getLogger(ArticleParser.class.getName());
    public ArticleParser(Article article) {
        super(article);
    }
    private VerseIntf verseIntf = ThreadStorage.getVerse();

//    public List<String> readTopicVerses() {
//        return readTopicVerses(title);
//    }

    @Override
    public List<String> readTopicVerses(String input) {
        VerseIntf verse = ThreadStorage.getVerse();
        List<String> result = new ArrayList<>();
        Pattern versePattern = getTopicVersePattern();
        Matcher m = versePattern.matcher(input);
//        int anchorPoint = getAnchorPointAfterTitle(title, input);
        int start = 0;
        while (m.find(start)) {
            String target = m.group();
            int position = m.end();
            start = position;
//            if (position > anchorPoint) {
//                break;
//            }
            result.add(replaceHtmlSpace(verse.appendNextCharTillCompleteVerse(input, target, position, input.length())));
        }
        return result;
    }

    @Override
    public List<String> readParagraphs() {

        String input = removeDoubleQuote(content);
        List<String> result = new ArrayList<>();
        if( StringUtils.isEmpty(input) ){
            return result;
        }

        String topic = getTopic();
        input = StringUtils.remove(input, topic);

        String[] lines= input.split("<br />");
        String paragraph = removeLinkBreak(removeHtmlTag(lines[0]));
        paragraph = paragraph.replaceAll("\\.", "."+pause(400));
        result.add(paragraph);

        return result;
    }

    @Override
    public String readThought() {
        return "";
    }

    @Override
    public String readPrayer() {
        return "";
    }

    @Override
    public String readEnd() {
        if( StringUtils.isEmpty(content) ){
            return "";
        }

        String input = removeDoubleQuote(content.replaceAll("<p>", "").replace("</p>", ""));
        String topic = getTopic();
        input = StringUtils.remove(input, topic);
        StringBuilder builder = new StringBuilder();
        String[] lines= input.split("<br />");

        for( int i = 1; i<lines.length; i++ ){
            String paragraph = removeHtmlTag(lines[i]);
            paragraph = paragraph.replaceAll("\\.", "."+pause(400));
            builder.append(paragraph);
        }

        return removeLinkBreak(builder.toString());
    }

    public String readFocusScripture() {
        VerseIntf verseIntf = ThreadStorage.getVerse();
        Pattern pattern = getFocusScripturePattern();
        Matcher m = pattern.matcher(title);
        while (m.find()) {
            String targe = m.group(2);
            String result = removeHtmlTag(replaceWithPause(replaceHtmlSpace(targe)));
            return verseIntf.convert(result);
        }

        return "";
    }

    public String getTopic(){
        String result = "";
        String input = removeDoubleQuote(content.replaceAll("<p>", "").replace("</p>", ""));
        for( char c : input.toCharArray() ){

            if( c == ' '
                    || c == '\''
                    || PunctuationTool.getAllowedPunctuations().contains(String.valueOf(c))
                    || System.lineSeparator().equals(String.valueOf(c)) ){
                result += c;
                continue;
            }
            if( Character.isUpperCase(c) ){
                result += c;
                continue;
            }

            if( c == '.' ){
                result += c;
                break;
            }
            break;
        }

        return StringUtils.trim(result);
    }

    @Override
    protected Pattern getFocusScripturePattern() {
        return Pattern.compile("([“|\"])(.*)([”|\"])");
    }
    
    @Override
    protected Pattern getDatePattern() {
        return Pattern.compile("\\b(?:January|February|March|April|May|June|July|August|September|October|November|December)\\s+(\\d{1,2})\\s+\\b");
    }

    @Override
    public Pattern getTopicVersePattern() {
        return verseIntf.getVersePattern();
    }
    
}
