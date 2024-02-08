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

import static com.cpbpc.comms.TextUtil.removeHtmlTag;

public class ArticleParser extends AbstractArticleParser {
    private static Logger logger = Logger.getLogger(ArticleParser.class.getName());
    public ArticleParser(Article article) {
        super(article);
    }
    private VerseIntf verseIntf = ThreadStorage.getVerse();

    public List<String> readTopicVerses() {
        return readTopicVerses(title);
    }

    @Override
    public List<String> readParagraphs() {

        List<String> result = new ArrayList<>();
        if( StringUtils.isEmpty(content) ){
            return result;
        }

        String[] lines= content.split("<br />");
        result.add(lines[0]);

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

        StringBuilder builder = new StringBuilder();
        String[] lines= content.split("<br />");

        for( int i = 1; i<lines.length; i++ ){
            builder.append(lines[i]);
        }

        return builder.toString();
    }

    public String readFocusScripture() {
        VerseIntf verseIntf = ThreadStorage.getVerse();
        Pattern pattern = getFocusScripturePattern();
        Matcher m = pattern.matcher(title);
        if (m.find()) {
            String targe = m.group(2);
            String result = removeHtmlTag(replaceWithPause(replaceSpace(targe)));
            return verseIntf.convert(result);
        }

        return "";
    }

    public String getTopic(){
        String result = "";
        for( char c : content.toCharArray() ){

            if( c == ' '
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
        return Pattern.compile("(“)(.*)(”)");
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
