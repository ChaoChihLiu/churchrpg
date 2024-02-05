package com.cpbpc.dailydevotion;

import com.cpbpc.comms.AbstractArticleParser;
import com.cpbpc.comms.AbstractComposer;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.ComposerResult;
import com.cpbpc.comms.PunctuationTool;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import com.cpbpc.rpgv2.en.BibleVerseGrab;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static com.cpbpc.comms.PunctuationTool.pause;

public class Composer extends AbstractComposer {
    private static final Properties appProperties = AppProperties.getConfig();

    private Logger logger = Logger.getLogger(Composer.class.getName());

    private static VerseIntf verse = ThreadStorage.getVerse();
    public Composer(AbstractArticleParser parser) {
        super(parser);
    }

    public List<ComposerResult> toTTS(boolean fixPronu, String publishDate) {
        List<ComposerResult> result = new ArrayList<>();

        Map<String, String> scripts = splitPolly(fixPronu);
        Set<Map.Entry<String, String>> entries = scripts.entrySet();
        for( Map.Entry<String, String> entry : entries ){
            ComposerResult composerResult = new ComposerResult();
            result.add(composerResult);

            String fileName = entry.getKey();
            String script = entry.getValue();

            composerResult.setScript(script);
            composerResult.setFileName(fileName);
            composerResult.addTags(sendToTTS(fileName, wrapToPolly(prettyPrintln(script)), publishDate));
        }

        return result;
    }

    @Override
    protected Map<String, String> splitPolly(boolean fixPronu) {
        Map<String, String> scripts = new LinkedHashMap<>();
        int scriptCounter = 1;

        StringBuilder buffer = new StringBuilder();

        buffer.append(parser.readDate()).append(pause(200))
                .append(parser.getArticle().getTiming()).append(PunctuationTool.pause(1600))
                .append(parser.readFocusScripture()).append(PunctuationTool.pause(800));
        scripts.put(scriptCounter+"_start", buffer.toString());
        scriptCounter++;

        int count = 0;
        try {
            for (String ref : parser.readTopicVerses(parser.getTitle())) {
                buffer = new StringBuilder();
                count++;
                List<String> refs = verse.analyseVerse(ref);
                List<String> verseContents = grabAndSplitVerse(BibleVerseGrab.grab(refs.get(0), refs.get(1)));
                buffer.append("The Bible passage is from")
                        .append(processSentence(" "+verse.convert(ref), fixPronu))
                ;
                for( String verseContent : verseContents ){
                    if(verseContents.indexOf(verseContent) == 0){
                        buffer.append(processSentence(verseContent, fixPronu));
                        scripts.put(scriptCounter+"_biblePassage_"+count+"_"+(verseContents.indexOf(verseContent)+1), buffer.toString());
                    }else{
                        scripts.put(scriptCounter+"_biblePassage_"+count+"_"+(verseContents.indexOf(verseContent)+1), processSentence(verseContent, fixPronu));
                    }
                    scriptCounter++;
                }
            }
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }

        buffer = new StringBuilder();
        buffer.append("This devotion is entitled").append(pause(1600))
                .append(processSentence(parser.getTopic(), fixPronu)).append(pause(400))
        ;
        scripts.put(scriptCounter+"_startDevotion", buffer.toString());
        scriptCounter++;

        for (String paragraph : parser.readParagraphs()) {
            buffer = new StringBuilder();
            buffer.append(processSentence(paragraph, fixPronu)).append(pause(400));
            scripts.put(scriptCounter+"_paragraph_"+(parser.readParagraphs().indexOf(paragraph)+1), buffer.toString());
            scriptCounter++;
        }

        buffer = new StringBuilder();
        buffer.append(pause(400)).append(processSentence(parser.readEnd(), fixPronu));
        scripts.put(scriptCounter+"_end", buffer.toString());
        scriptCounter++;

        return scripts;
    }

    public String getPublishDate(String input){
        String publishDate = input.split(" ")[1];
        return publishDate;
    }

    public String getPublishMonth(String input){
        String publishMonth = input.split(" ")[0];
        return publishMonth;
    }

}
