package com.cpbpc.rpgv2.zh;

import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.AbstractArticleParser;
import com.cpbpc.rpgv2.AbstractComposer;
import com.cpbpc.rpgv2.ConfigObj;
import com.cpbpc.rpgv2.VerseIntf;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cpbpc.comms.PunctuationTool.pause;

public class Composer extends AbstractComposer {
    public Composer(AbstractArticleParser parser) {
        super(parser);
    }
    
    private VerseIntf verse = ThreadStorage.getVerse();

    protected String mapBookAbbre(String book) {

        if (null == book || book.trim().length() <= 0) {
            return book;
        }

        book = book.replace(".", "");

        Map<String, ConfigObj> verseMap = verse.getVerseMap();
        Set<Map.Entry<String, ConfigObj>> entries = verseMap.entrySet();
        for (Map.Entry<String, ConfigObj> entry : entries) {
            ConfigObj obj = entry.getValue();
            if (StringUtils.equalsIgnoreCase(book, obj.getShortForm())) {
                return obj.getFullWord();
            }
        }

        return book;
    }

    @Override
    protected String toPolly(boolean fixPronu) {
        StringBuilder result = new StringBuilder();

        result.append(parser.readDate()).append(pause(200));
        result.append("今日灵修题目").append(pause(200))
                .append(processSentence(parser.getTitle(), fixPronu)).append(pause(400))
        ;

        int count = 0;

        try {
            for (String ref : parser.readTopicVerses()) {
                List<String> refs = verse.analyseVerse(ref);
                String book = refs.get(0);
                for( int i=1; i<refs.size(); i++ ){
                    count++;
                    result.append("圣经经文第" + count + "段").append(pause(200))
                            .append(processSentence(verse.convert(makeCompleteVerse(book, refs.get(1), refs.get(i))), fixPronu)).append(pause(400))
                            .append(processSentence(BibleVerseScraper.scrap(mapBookAbbre(book), makeCompleteVerse(refs.get(1), refs.get(i))), fixPronu))
                    ;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.append(pause(800)).append("結束经文朗读").append(pause(800));
        result.append("引用经文").append(pause(200))
                .append(processSentence(parser.readFocusScripture(), fixPronu)).append(pause(400));

        result.append("今日灵修题目").append(pause(200))
                .append(processSentence(parser.getTitle(), fixPronu)).append(pause(800))
        ;

        for (String paragraph : parser.readParagraphs()) {
            result.append(processSentence(paragraph, fixPronu)).append(pause(400));
        }
        result.append(pause(400)).append(processSentence(parser.readThought(), fixPronu));
        result.append(pause(800)).append(processSentence(parser.readPrayer(), fixPronu));


        try {
//            return wrapToPolly(result.toString());
            return prettyPrintln(wrapToPolly(result.toString()));
//            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String makeCompleteVerse(String reference, String verse) {

        if( StringUtils.equals(reference, verse) ){
            return reference;
        }

        if( StringUtils.contains(verse, "章") || StringUtils.contains(verse, "篇") ){
            return verse;
        }

        if( !StringUtils.contains(reference, "章") && !StringUtils.contains(verse, "篇") ){
            return verse;
        }

        String chapter = "";
        if(StringUtils.contains(reference, "章")){
            chapter = StringUtils.substring(reference, 0, StringUtils.indexOf(reference, "章")+1);
        }
        if(StringUtils.contains(reference, "篇")){
            chapter = StringUtils.substring(reference, 0, StringUtils.indexOf(reference, "篇")+1);
        }

        return chapter + verse;
    }
    private String makeCompleteVerse(String book, String reference, String verse) {

        if( StringUtils.equals(reference, verse) ){
            return book + reference;
        }

        if( StringUtils.contains(verse, "章") || StringUtils.contains(verse, "篇") ){
            return book + verse;
        }

        if( !StringUtils.contains(reference, "章") && !StringUtils.contains(verse, "篇") ){
            return book + verse;
        }

        String chapter = "";
        if(StringUtils.contains(reference, "章")){
            chapter = StringUtils.substring(reference, 0, StringUtils.indexOf(reference, "章")+1);
        }
        if(StringUtils.contains(reference, "篇")){
            chapter = StringUtils.substring(reference, 0, StringUtils.indexOf(reference, "篇")+1);
        }

        return book + chapter + verse;
    }
}
