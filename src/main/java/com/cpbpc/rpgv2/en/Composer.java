package com.cpbpc.rpgv2.en;

import com.cpbpc.comms.AbstractArticleParser;
import com.cpbpc.comms.AbstractComposer;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.ComposerResult;
import com.cpbpc.comms.RomanNumeral;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static com.cpbpc.comms.NumberConverter.ordinal;
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
            composerResult.addTags(sendToTTS(fileName, wrapToPolly(prettyPrintln(script)), publishDate, parser.getArticle().getCounter()));
        }

        return result;
    }

    @Override
    protected Map<String, String> splitPolly(boolean fixPronu) {
        Map<String, String> scripts = new LinkedHashMap<>();
        int scriptCounter = 1;

        StringBuilder buffer = new StringBuilder();
        
        buffer.append(findWeekDay(parser.getArticle().getStartDate()))
                .append(parser.readDate())
                .append(pause(200));
        buffer.append("Today's devotional is entitled").append(pause(200))
                .append(processSentence(RomanNumeral.convert(parser.getTitle(), true), fixPronu)).append(pause(400))
        ;
        scripts.put(scriptCounter+"_start", buffer.toString());
        scriptCounter++;

        int count = 0;
        try {
            for (String ref : parser.readTopicVerses()) {
                buffer = new StringBuilder();
                count++;
                List<String> refs = verse.analyseVerse(removeLineWhitespace(ref));
                List<String> verseContents = grabAndSplitVerse(BibleVerseGrab.grab(refs.get(0), refs.get(1), false));
                buffer.append("The " + ordinal(count) + " Bible passage for today is").append(pause(200))
                        .append(processSentence(" "+verse.convert(ref), fixPronu)).append(pause(400))
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
        buffer.append(pause(800)).append("End of scripture reading").append(pause(800));
        buffer.append("The scripture passage in focus is").append(pause(200))
                .append(processSentence(parser.readFocusScripture(), fixPronu)).append(pause(400));

        buffer.append("Today's devotional is entitled").append(pause(200))
                .append(processSentence(RomanNumeral.convert(parser.getTitle(), true), fixPronu)).append(pause(800))
        ;
        scripts.put(scriptCounter+"_startRPG", buffer.toString());
        scriptCounter++;

        for (String paragraph : parser.readParagraphs()) {
            buffer = new StringBuilder();
            buffer.append(processSentence(paragraph, fixPronu)).append(pause(400));
            scripts.put(scriptCounter+"_paragraph_"+(parser.readParagraphs().indexOf(paragraph)+1), buffer.toString());
            scriptCounter++;
        }

        buffer = new StringBuilder();
        buffer.append(pause(400)).append(processSentence(parser.readThought(), fixPronu));
        buffer.append(pause(800)).append(processSentence(parser.readPrayer(), fixPronu));
        scripts.put(scriptCounter+"_end", buffer.toString());
        scriptCounter++;

//        String result = buffer.toString();
//
//        try {
//              String script = prettyPrintln(wrapToPolly(result));
//              sendToPolly(script, publishDate);
////            return wrapToPolly(result.toString());
//            return script;
////            return result.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return "";
        return scripts;
    }

//    @Override
//    private void sendToPolly(String content, String publishDate){
//        logger.info("use.polly is " + Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "true")));
//        if (Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "false")) != true) {
//            return;
//        }
//        logger.info("send to polly script S3 bucket!");
//        AWSUtil.putScriptToS3(content, publishDate);
//    }
}
