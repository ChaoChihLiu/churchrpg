package com.cpbpc.rpgv2.zh;

import com.cpbpc.rpgv2.AbstractArticleParser;
import com.cpbpc.rpgv2.AbstractComposer;
import com.cpbpc.rpgv2.ConfigObj;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cpbpc.rpgv2.util.PauseTool.pause;

public class Composer extends AbstractComposer {
    public Composer(AbstractArticleParser parser) {
        super(parser);
    }

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
    protected String toPolly() {
        StringBuilder result = new StringBuilder();

        result.append(parser.readDate()).append(pause(200));
        result.append("今日靈修題目").append(pause(200))
                .append(processSentence(parser.getTitle())).append(pause(400))
        ;

        int count = 0;

        try {
            for (String ref : parser.readTopicVerses()) {
                count++;
                List<String> refs = parser.analyseVerse(ref);
                result.append("聖經經文第" + count + "段").append(pause(200))
                        .append(processSentence(parser.convertVerse(ref))).append(pause(400))
                        .append(processSentence(BibleVerseScraper.scrap(mapBookAbbre(refs.get(0)), refs.get(1)))).append(pause(400))
                ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.append("結束經文朗讀").append(pause(400));
        result.append("引用經文").append(pause(200))
                .append(processSentence(parser.readFocusScripture())).append(pause(400));

        result.append("今日靈修題目").append(pause(200))
                .append(processSentence(parser.getTitle())).append(pause(400))
        ;

        for (String paragraph : parser.readParagraphs()) {
            result.append(processSentence(paragraph)).append(pause(400));
        }
        result.append(pause(400)).append(processSentence(parser.readThought()));
        result.append(pause(800)).append(processSentence(parser.readPrayer()));


        try {
//            return wrapToPolly(result.toString());
            return prettyPrintln(wrapToPolly(result.toString()));
//            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
