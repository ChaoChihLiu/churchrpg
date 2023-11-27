package com.cpbpc.rpgv2.en;

import com.cpbpc.comms.RomanNumeral;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.AbstractArticleParser;
import com.cpbpc.rpgv2.AbstractComposer;
import com.cpbpc.rpgv2.VerseIntf;

import java.util.List;

import static com.cpbpc.comms.NumberConverter.ordinal;
import static com.cpbpc.comms.PunctuationTool.pause;

public class Composer extends AbstractComposer {

    private static VerseIntf verse = ThreadStorage.getVerse();
    public Composer(AbstractArticleParser parser) {
        super(parser);
    }

    @Override
    protected String toPolly() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(parser.readDate()).append(pause(200));
        buffer.append("Today's devotional is entitled").append(pause(200))
                .append(processSentence(RomanNumeral.convert(parser.getTitle()))).append(pause(400))
        ;

        int count = 0;
        try {
            for (String ref : parser.readTopicVerses()) {
                count++;
                List<String> refs = verse.analyseVerse(ref);
                buffer.append("The " + ordinal(count) + " Bible passage for today is").append(pause(200))
                        .append(processSentence(verse.convert(ref))).append(pause(400))
                        .append(processSentence(BibleVerseScraper.scrap(refs.get(0), refs.get(1))))
                ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.append("End of scripture reading").append(pause(400));
        buffer.append("The scripture passage in focus is").append(pause(200))
                .append(processSentence(parser.readFocusScripture())).append(pause(400));

        buffer.append("Today's devotional is entitled").append(pause(200))
                .append(processSentence(parser.getTitle())).append(pause(400))
        ;

        for (String paragraph : parser.readParagraphs()) {
            buffer.append(processSentence(paragraph)).append(pause(400));
        }
        buffer.append(pause(400)).append(processSentence(parser.readThought()));
        buffer.append(pause(800)).append(processSentence(parser.readPrayer()));

        String result = buffer.toString();

        try {
//            return wrapToPolly(result.toString());
            return prettyPrintln(wrapToPolly(result));
//            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }
}
