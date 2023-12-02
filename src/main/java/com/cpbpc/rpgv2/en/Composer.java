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
    protected String toPolly(boolean fixPronu) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(parser.readDate()).append(pause(200));
        buffer.append("Today's devotional is entitled").append(pause(200))
                .append(processSentence(RomanNumeral.convert(parser.getTitle()), fixPronu)).append(pause(400))
        ;

        int count = 0;
        try {
            for (String ref : parser.readTopicVerses()) {
                count++;
                List<String> refs = verse.analyseVerse(ref);
                buffer.append("The " + ordinal(count) + " Bible passage for today is").append(pause(200))
                        .append(processSentence(verse.convert(ref), fixPronu)).append(pause(400))
                        .append(processSentence(BibleVerseScraper.scrap(refs.get(0), refs.get(1)), fixPronu))
                ;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        buffer.append(pause(800)).append("End of scripture reading").append(pause(800));
        buffer.append("The scripture passage in focus is").append(pause(200))
                .append(processSentence(parser.readFocusScripture(), fixPronu)).append(pause(400));

        buffer.append("Today's devotional is entitled").append(pause(200))
                .append(processSentence(parser.getTitle(), fixPronu)).append(pause(800))
        ;

        for (String paragraph : parser.readParagraphs()) {
            buffer.append(processSentence(paragraph, fixPronu)).append(pause(400));
        }
        buffer.append(pause(400)).append(processSentence(parser.readThought(), fixPronu));
        buffer.append(pause(800)).append(processSentence(parser.readPrayer(), fixPronu));

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
