package com.cpbpc.pdf.rpg.zh;


import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static Pattern date_pattern = Pattern.compile("(?:一|二|三|四|五|六|七|八|九|十|十一|十二)月[一|二|三|四|五|六|七|八|九|十]{1,3}日，(礼拜|星期|主)(?:一|二|三|四|五|六|日)");
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
        Matcher m = versePattern.matcher(content);
        int anchorPoint = getAnchorPointAfterDate();
        int start = anchorPoint;
        while (m.find(start)) {
            String target = m.group();
            int position = m.end();
            start = position;
            result.add(verse.appendNextCharTillCompleteVerse(content, target, position, content.length()));
        }
        return result;
    }

    public List<String> readFocusScripture() {
        List<String> result = new ArrayList<>();
        List<String> verses = readTopicVerses();
        int anchorPoint = getAnchorPointAfterDate();
        int start = anchorPoint;

        String focusWords = StringUtils.substring(content, anchorPoint, content.length());
        for( String verse : verses ){
            focusWords = StringUtils.replace(focusWords, verse, "");
        }

        result.addAll(List.of(StringUtils.split(focusWords, System.lineSeparator())));
        result.removeIf(s -> StringUtils.isEmpty(StringUtils.trim(s)));
//        Iterator<String> iterator = result.iterator();
//        while (iterator.hasNext()) {
//            String element = iterator.next();
//            if (StringUtils.isEmpty(element)) {
//                iterator.remove();
//            }
//        }

        return result;
    }

    private static Pattern end_pattern = Pattern.compile("[\\u4E00-\\u9FFF]{2}\\s{0,}：\\s{0,}");
    public Map<String, String> readEnd(){

        Map<String, String> result = new LinkedHashMap<>();
        String to_be_searched = StringUtils.substring(content, 0, StringUtils.indexOf(content, System.lineSeparator()+title+System.lineSeparator()));
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
            result.put( startings.get(i), StringUtils.remove(StringUtils.substring(to_be_searched, start_point, end_point), System.lineSeparator()) );
        }

        return result;
    }

    private Pattern para_pattern = Pattern.compile("[。|?|!|？|！|…]" + System.lineSeparator());
    public List<String> readParagraphs(){
        List<String> paragraphs = new ArrayList<>();
        String date = readDate();

        int start = StringUtils.indexOf(content, title) + title.length();
        int end = StringUtils.indexOf(content, date);

        String to_be_searched = StringUtils.substring(content, start, end);
        Matcher matcher = para_pattern.matcher(to_be_searched);
        int start_point = 0;
        while( matcher.find(start_point) ){
            int index = matcher.end();
            paragraphs.add( StringUtils.remove(StringUtils.substring(to_be_searched, start_point, index), System.lineSeparator()) );
            start_point = index;
        }

        paragraphs.removeIf(s -> StringUtils.isEmpty(StringUtils.trim(s)));

//        List<String> modifiedList = paragraphs.stream()
//                .map(line -> StringUtils.remove(line, System.lineSeparator())) // for example, converting to uppercase
//                .collect(Collectors.toList());

//        Iterator<String> iterator = paragraphs.stream().iterator();
//        while (iterator.hasNext()) {
//            String line = iterator.next();
//            line = StringUtils.remove(line, System.lineSeparator());
//        }

        return paragraphs;
    }

}
