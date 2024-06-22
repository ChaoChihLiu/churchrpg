package com.cpbpc.dailydevotion;

import com.cpbpc.comms.AbstractArticleParser;
import com.cpbpc.comms.Article;
import com.cpbpc.comms.TextUtil;
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

        int anchorPoint = findMeditationStartPoint(input);
        String content_splitted = StringUtils.substring(input, 0, anchorPoint);

        String topic = getTopic();
        content_splitted = StringUtils.remove(content_splitted, topic);

        String[] lines= content_splitted.split("<br />");
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

    //For <strong>Meditation
    private static Pattern meditationPattern = Pattern.compile("For\\s{0,}(<strong>){0,}\\s{0,}Meditation");
    private int findMeditationStartPoint(String input){
        if( StringUtils.isEmpty(input) ){
            return 0;
        }

        int result = 0;
        Matcher matcher = meditationPattern.matcher(input);
        if( matcher.find() ){
            result = matcher.start();
        }

        return result;
    }
    private int findMeditationEndPoint(String input){
        if( StringUtils.isEmpty(input) ){
            return 0;
        }

        int result = 0;
        Matcher matcher = meditationPattern.matcher(input);
        if( matcher.find() ){
            result = matcher.end();
        }

        return result;
    }

    @Override
    public String readEnd() {
        if( StringUtils.isEmpty(content) ){
            return "";
        }
        int anchorPoint = findMeditationEndPoint(content);
        String content_splitted = StringUtils.substring(content, anchorPoint, content.length());

        String input = removeDoubleQuote(content_splitted.replaceAll("<p>", "").replace("</p>", ""));
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

    protected Pattern getTopicPattern() {
//        return Pattern.compile("([“|\"]{0,})([A-Z\\s\\?,;:]*)([”|\"]{0,})");
        return Pattern.compile("[A-Z\\s\\?,;:'‘]+");
    }
    public String getTopic(){
        String result = "";
//        List<String> allowedPunctuations = PunctuationTool.getAllowedPunctuations();
//        allowedPunctuations.add("?");
//        allowedPunctuations.add("'");
//        String input = removeDoubleQuote(content.replaceAll("<p>", "").replace("</p>", ""));
//        for( char c : input.toCharArray() ){
//
//            if( c == ' '
//                    || allowedPunctuations.contains(String.valueOf(c))
//                    || System.lineSeparator().equals(String.valueOf(c)) ){
//                result += c;
//                continue;
//            }
//            if( Character.isUpperCase(c) ){
//                result += c;
//                continue;
//            }
//
//            if( c == '.' ){
//                result += c;
//                break;
//            }
//            break;
//        }
        String input = content.replaceAll("<p>", "").replace("</p>", "");
        Pattern pattern = getTopicPattern();
        Matcher matcher = pattern.matcher(input);
        if( matcher.find() ){
            result = matcher.group();
        }

        return StringUtils.trim(result);
    }

    @Override
    protected Pattern getFocusScripturePattern() {
        return Pattern.compile("([“|\"])(.*)([”|\"])");
    }
    
    @Override
    protected Pattern getDatePattern() {
        return TextUtil.getDatePattern();
    }

    @Override
    public Pattern getTopicVersePattern() {
        return verseIntf.getVersePattern();
    }
    
}
