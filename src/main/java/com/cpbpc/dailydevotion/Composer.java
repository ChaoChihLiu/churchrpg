package com.cpbpc.dailydevotion;

import com.amazonaws.util.StringUtils;
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
//            composerResult.addTags(sendToTTS(fileName, wrapToPolly(prettyPrintln(script)), publishDate));
            composerResult.addTags(sendToTTS(fileName, script, publishDate));
        }

        return result;
    }

    @Override
    protected Map<String, String> splitPolly(boolean fixPronu) {
        String voiceId = AppProperties.getConfig().getProperty(StringUtils.lowerCase(parser.getArticle().getTiming())+"_voice_id");

        Map<String, String> scripts = new LinkedHashMap<>();
        int scriptCounter = 1;

        StringBuilder buffer = new StringBuilder();

        buffer.append(parser.getArticle().getStartDate()).append(pause(200))
                .append(parser.getArticle().getTiming()).append(PunctuationTool.pause(400))
                .append(parser.readFocusScripture()).append(PunctuationTool.pause(800));
        scripts.put(scriptCounter+"_start", wrapToAzure(prettyPrintln(buffer.toString()), voiceId));
        scriptCounter++;

        int count = 0;
        try {
            for (String ref : parser.readTopicVerses(parser.getTitle())) {
                buffer = new StringBuilder();
                count++;
                List<String> refs = verse.analyseVerse(ref);
                List<String> verseContents = grabAndSplitVerse(BibleVerseGrab.grab(refs.get(0), refs.get(1)));
                buffer.append("The Bible passage is from")
                        .append(processRemSentence(" "+verse.convert(ref), fixPronu))
                        .append(PunctuationTool.pause(400))
                ;
                for( String verseContent : verseContents ){
                    if(verseContents.indexOf(verseContent) == 0){
                        buffer.append(processRemSentence(verseContent, fixPronu));
                        scripts.put(scriptCounter+"_biblePassage_"+count+"_"+(verseContents.indexOf(verseContent)+1), wrapToAzure(prettyPrintln(buffer.toString()), voiceId));
                    }else{
                        scripts.put(scriptCounter+"_biblePassage_"+count+"_"+(verseContents.indexOf(verseContent)+1), wrapToAzure(prettyPrintln(processRemSentence(verseContent, fixPronu)), voiceId));
                    }
                    scriptCounter++;
                }
            }
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }

        buffer = new StringBuilder();
        buffer.append("This devotion is entitled").append(pause(800))
                .append(processRemSentence(parser.getTopic(), fixPronu)).append(pause(400))
        ;
        scripts.put(scriptCounter+"_startDevotion", wrapToAzure(prettyPrintln(buffer.toString()), voiceId));
        scriptCounter++;

        for (String paragraph : parser.readParagraphs()) {
            buffer = new StringBuilder();
            buffer.append(processRemSentence(paragraph.replace(parser.getTopic(), ""), fixPronu)).append(pause(400));
            scripts.put(scriptCounter+"_paragraph_"+(parser.readParagraphs().indexOf(paragraph)+1), wrapToAzure(prettyPrintln(buffer.toString()), voiceId));
            scriptCounter++;
        }

        buffer = new StringBuilder();
        buffer.append(pause(200)).append("For prayer: ");
        buffer.append(pause(400)).append(processRemSentence(parser.readEnd(), fixPronu));
        scripts.put(scriptCounter+"_end", wrapToAzure(prettyPrintln(buffer.toString()), voiceId));
        scriptCounter++;

        return scripts;
    }

    private String processRemSentence(String content, boolean fixPronu){
        return this.processSentence(content, fixPronu, false);
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
