package com.cpbpc.pdf.rpg.zh;


import com.cpbpc.comms.TextUtil;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cpbpc.comms.TextUtil.escapeSpecialChar;

public class ArticleParser {

    private String content = "";
    private String title = "";
    private VerseIntf verseReg = ThreadStorage.getVerse();

    public ArticleParser(String input, String title){
        this.content = input;
        this.title = title;
    }

    public String getTitle(){
        return title;
    }

    private static Pattern date_pattern = Pattern.compile("(?:一|二|三|四|五|六|七|八|九|十|十一|十二)月[一|二|三|四|五|六|七|八|九|十]{1,3}日{0,}[（上）|（下）]{0,}，(礼拜|星期|主)(?:一|二|三|四|五|六|日)");
    public String readDate(){
        Matcher matcher = date_pattern.matcher(content);

        while (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private int getAnchorPointAfterDate(){
        String date = readDate();
        return StringUtils.indexOf(content, date)+date.length();
    }

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

    public List<String> readTopicVerses() {
        VerseIntf verse = ThreadStorage.getVerse();
        List<String> result = new ArrayList<>();
        Pattern versePattern = getTopicVersePattern();
        int anchorPoint = getAnchorPointAfterDate();
        String contentAfterDate = StringUtils.replace(
                                                    StringUtils.substring(content, anchorPoint, content.length()),
                                                    StringUtils.SPACE, System.lineSeparator());
        Matcher m = versePattern.matcher(contentAfterDate);
        int start = 0;
        while (m.find(start)) {
            String target = m.group();
            int position = m.end();
            start = position;
            String verseRef = verse.appendNextCharTillCompleteVerse(contentAfterDate, target, position, contentAfterDate.length());
            result.add(verseRef);
        }
        return result;
    }

    public List<String> readFocusScripture() {
        List<String> result = new ArrayList<>();
        result.addAll(parse1stFocusScripturePattern());
        if( !result.isEmpty() ){
            return result;
        }
        result.addAll(parse2ndFocusScripturePattern());
        return result;
    }

    /*
    “……我的肉身也要安然居住。”
    七月十四日，礼拜一
    路得记二章4-7节
    诗篇三十七篇3-7节
     */
    //but sometimes it may be on 1 line
    private Collection<String> parse2ndFocusScripturePattern() {
        int anchorPoint = getAnchorPointAfterDate();

        List<String> result = new ArrayList<>();
        String regex = "“([\\s\\S]*?)”";
        Pattern secondPattern = Pattern.compile(regex);
        Matcher m = secondPattern.matcher(content);
        int previousMatchedPosition = 0;
        String matched = "";
        while( m.find() ){
            String line = m.group();
            int currentMatchedPosition = m.start();
            if( currentMatchedPosition >= anchorPoint ){
                break;
            }

            if( currentMatchedPosition > previousMatchedPosition ){
                matched = line;
            }
        }
        if( !StringUtils.isEmpty(matched) ){
            result.add(matched);
        }

        return result;
    }

    /*
        “…就可以喜乐，
        五月二十日，礼拜一
        腓立比书二章25节至28节
        罗马书十二章9节至21节 我也可以少些忧愁。”
         */
    /*
    六月二十九日，主日傍晚 “……祭司就要估定价值。
    利未记二十七章 房屋是好是坏，祭司怎样估定，
    传道书十章12节 就要以怎样为定。”
     */
    //sometimes focus scripture mixed with topic verses
    private Collection<String> parse1stFocusScripturePattern() {

        List<String> result = new ArrayList<>();
        List<String> verses = readTopicVerses();

        /*
        “……我的肉身也要安然居住。”
        七月十四日，礼拜一
        路得记二章4-7节
        诗篇三十七篇3-7节
         */
        //special case handling
        result.addAll(returnFocusScriptureGoBeforeDate());
        if( !result.isEmpty() ) {
            result.removeIf(s -> StringUtils.isEmpty(StringUtils.trim(s)));
            result.replaceAll(s -> StringUtils.trim(s));
            return result;
        }

        int anchorPoint = getAnchorPointAfterDate();

        String focusWords = StringUtils.substring(content, anchorPoint, content.length());
        for( String verse : verses ){
            focusWords = StringUtils.remove(focusWords, verse);
            focusWords = StringUtils.trim(focusWords);
        }

        result.addAll(List.of(StringUtils.split(focusWords, System.lineSeparator())));
        result.removeIf(s -> StringUtils.isEmpty(StringUtils.trim(s)));
        result.replaceAll(s -> StringUtils.trim(s));
        return result;
    }

    //^(“.*?)(五月二十日，礼拜一)\s*(腓立比书二章25节至28节)\s*(罗马书十二章9节至21节)(.*”)
    private List<String> returnFocusScriptureGoBeforeDate() {
        String date = readDate();
        List<String> topicVerses = readTopicVerses();
        StringBuffer buffer = new StringBuffer("(“[^“”]*)");
        buffer.append(date).append("\\s*");
        for( String verse : topicVerses ){
            buffer.append(verse);
            int index = topicVerses.indexOf(verse);
            if( index < topicVerses.size()-1 ){
                buffer.append("\\s*");
            }
        }
        buffer.append("([^“”]*”{0,})");

        List<String> list = new ArrayList<>();
//        Pattern p = Pattern.compile("“([^“”]*)五月二十日，礼拜一\\s*腓立比书二章25节至28节\\s*罗马书十二章9节至21节([^“”]*)”");
        Pattern p = Pattern.compile(buffer.toString());
        Matcher matcher = p.matcher(content);
        while ( matcher.find() ){
            list.add(matcher.group(1));
            list.add(matcher.group(2));
        }

        return list;
    }

    //    private static Pattern end_pattern = Pattern.compile("^[\\u4E00-\\u9FFF]{2}\\s{0,}：\\s{0,}");
private static Pattern end_pattern = Pattern.compile("[默想|祷告|背诵]{2}\\s{0,}：\\s{0,}");
    public Map<String, String> readEnd(){

        int anchorPoint = getAnchorPointBeforeTitle();
        Map<String, String> result = new LinkedHashMap<>();
//        String to_be_searched = StringUtils.substring(content, 0, StringUtils.indexOf(content, System.lineSeparator()+title+System.lineSeparator()));
        String to_be_searched = StringUtils.substring(content, 0, anchorPoint);
        Matcher matcher = end_pattern.matcher(to_be_searched);

        List<String> startings = new ArrayList<>();
        while( matcher.find() ){
            startings.add(matcher.group());
        }

        for( int i=0; i<startings.size(); i++ ){
            int start_point = StringUtils.indexOf(to_be_searched, startings.get(i)) + startings.get(i).length();
            int end_point = 0;
            if( (i+1) >= startings.size() ){
                end_point = to_be_searched.length();
            }else{
                end_point = StringUtils.indexOf(to_be_searched, startings.get(i+1));
            }
//            result.add( StringUtils.remove(StringUtils.substring(to_be_searched, start_point, end_point), System.lineSeparator()) );
            result.put( StringUtils.trim(startings.get(i)),
                    StringUtils.trim(StringUtils.remove(StringUtils.substring(to_be_searched, start_point, end_point), System.lineSeparator())) );
        }

        return result;
    }

    private String removeEnd(String input){
        if( StringUtils.isEmpty(input) ){
            return input;
        }

        Map<String, String> ends = readEnd();
        String result = input;
        for( String end : ends.values() ){
            result = result.replaceAll(TextUtil.escapeSpecialChar(end), "");
        }

        return result;
    }
    private String removeFocusedScripture(String input){
        if( StringUtils.isEmpty(input) ){
            return input;
        }

        List<String> scriptures = readFocusScripture();
        String result = input;
        for( String scripture : scriptures ){
            result = result.replaceAll(TextUtil.escapeSpecialChar(scripture), "");
        }

        return result;
    }

    private Pattern para_pattern = Pattern.compile("[。|。”|?|!|？|！|…|)|）|]" + System.lineSeparator());
    public List<String> readParagraphs(){
        List<String> paragraphs = new ArrayList<>();
        String date = readDate();

        int start = getAnchorPointAfterTitle();
        int end = StringUtils.indexOf(content, date);

        String to_be_searched = StringUtils.trim(StringUtils.substring(content, start, end));

        to_be_searched = removeEnd(to_be_searched);
        to_be_searched = removeFocusedScripture(to_be_searched);
//        List<String> focusScriptures = readFocusScripture();
//        for( String scripture : focusScriptures ){
//            if( !to_be_searched.endsWith(System.lineSeparator() + scripture) ){
//                 continue;
//            }
//            to_be_searched = StringUtils.substring(to_be_searched, StringUtils.indexOf(to_be_searched, scripture), to_be_searched.length());
////            to_be_searched = StringUtils.remove(to_be_searched, StringUtils.trim(scripture));
//        }

        Matcher matcher = para_pattern.matcher(to_be_searched);
        int start_point = 0;
        while( matcher.find(start_point) ){
            int index = matcher.end();
//            paragraphs.add( StringUtils.remove(StringUtils.substring(to_be_searched, start_point, index), System.lineSeparator()) );
            paragraphs.add( StringUtils.substring(to_be_searched, start_point, index) );
            start_point = index;
        }

        List<String> focusedScriptures = readFocusScripture();
        String para = paragraphs.get(paragraphs.size()-1);
        if( StringUtils.endsWith(to_be_searched, para) ){
            paragraphs.removeIf(s -> StringUtils.isEmpty(StringUtils.trim(StringUtils.remove(s, System.lineSeparator()))));
            paragraphs.replaceAll(s -> StringUtils.remove(s, System.lineSeparator()));
            return paragraphs;
        }

        start = StringUtils.indexOf(to_be_searched, para) + para.length();
        end = to_be_searched.length();
        paragraphs.add(StringUtils.substring(to_be_searched, start, end));

        paragraphs.removeIf(s -> StringUtils.isEmpty(StringUtils.trim(StringUtils.remove(s, System.lineSeparator()))));
        paragraphs.replaceAll(s -> StringUtils.remove(s, System.lineSeparator()));
        return paragraphs;
    }

    private int getAnchorPointAfterTitle() {
        String title_escaped = escapeSpecialChar(getTitle());
        Pattern title_pattern = Pattern.compile("\\R{0,}" + title_escaped + "\\s{0,}\\R{1,}");
        Matcher matcher = title_pattern.matcher(content);
        while( matcher.find() ){
            return matcher.end();
        }

        return 0;
    }

    private int getAnchorPointBeforeTitle() {
        String title_escaped = escapeSpecialChar(getTitle());
        Pattern title_pattern = Pattern.compile("\\R{1,}" + title_escaped + "\\s{0,}\\R{1,}");
        Matcher matcher = title_pattern.matcher(content);
        while( matcher.find() ){
            return matcher.start();
        }

        return 0;
    }

}
