package com.cpbpc.rpgv2.zh;

import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.TextUtil;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.AbstractArticleParser;
import com.cpbpc.rpgv2.AbstractComposer;
import com.cpbpc.rpgv2.ComposerResult;
import com.cpbpc.rpgv2.ConfigObj;
import com.cpbpc.rpgv2.VerseIntf;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static com.cpbpc.comms.PunctuationTool.pause;
import static com.cpbpc.comms.PunctuationTool.replaceCHPunctuationWithBreakTag;
import static com.cpbpc.comms.PunctuationTool.replacePauseTag;

public class Composer extends AbstractComposer {

    private static final Properties appProperties = AppProperties.getConfig();
    private static Logger logger = Logger.getLogger(Composer.class.getName());
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
    protected List<ComposerResult> toPolly(boolean fixPronu, String publishDate) {
        List<ComposerResult> result = new ArrayList<>();

        Map<String, String> scripts = splitPolly(fixPronu);
        scripts = houseKeepRedundantTag(scripts);
        Set<Map.Entry<String, String>> entries = scripts.entrySet();
        for( Map.Entry<String, String> entry : entries ){
            ComposerResult composerResult = new ComposerResult();
            result.add(composerResult);
            
            String fileName = entry.getKey();
            String script = entry.getValue();

            composerResult.setScript(script);
            composerResult.setFileName(fileName);
            composerResult.addTags(sendToPolly(fileName, wrapToPolly(prettyPrintln(script)), publishDate));
        }
        
        return result;
    }
    
    protected Map<String, String> splitPolly(boolean fixPronu) {
        Map<String, String> scripts = new LinkedHashMap<>();
        int scriptCounter = 1;

        StringBuilder result = new StringBuilder();
        result.append(parser.readDate()).append(pause(200));
        result.append("今日灵修题目").append(pause(200))
                .append(processSentence(parser.getTitle(), fixPronu)).append(pause(400))
        ;
        scripts.put(scriptCounter+"_start", result.toString());
        scriptCounter++;

        int count = 0;
        try {
            for (String ref : parser.readTopicVerses()) {
                List<String> refs = verse.analyseVerse(ref, parser.getTopicVersePattern());
                String book = refs.get(0);
                for( int i=1; i<refs.size(); i++ ){
                    result = new StringBuilder();
                    count++;
                    result.append("圣经经文第" + count + "段").append(pause(200))
                            .append(processSentence(verse.convert(makeCompleteVerse(ZhConverterUtil.toSimple(book), refs.get(1), refs.get(i)), parser.getTopicVersePattern()), fixPronu)).append(pause(400))
                            .append(processSentence(BibleVerseGrab.grab(mapBookAbbre(book), makeCompleteVerse(refs.get(1), refs.get(i))), fixPronu))
                    ;

                    scripts.put(scriptCounter+"_biblePassage_"+count, result.toString());
                    scriptCounter++;
                }
            }
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }

        result = new StringBuilder();
        result.append(pause(800)).append("結束经文朗读").append(pause(800));
        result.append("引用经文").append(pause(200))
                .append(processSentence(parser.readFocusScripture(), fixPronu)).append(pause(400));

        result.append("今日灵修题目").append(pause(200))
                .append(processSentence(parser.getTitle(), fixPronu)).append(pause(800))
        ;
        scripts.put(scriptCounter+"_startRPG", result.toString());
        scriptCounter++;

        for (String paragraph : parser.readParagraphs()) {
            result = new StringBuilder();
            result.append(processSentence(paragraph, fixPronu)).append(pause(400));
            scripts.put(scriptCounter+"_paragraph_"+(parser.readParagraphs().indexOf(paragraph)+1), result.toString());
            scriptCounter++;
        }

        result = new StringBuilder();
        result.append(pause(400)).append(processSentence(parser.readThought(), fixPronu));
        result.append(pause(800)).append(processSentence(parser.readPrayer(), fixPronu));
        scripts.put(scriptCounter+"_end", result.toString());
        scriptCounter++;

        return scripts;
    }

    private String makeCompleteVerse(String reference, String verse) {

        if( StringUtils.equals(reference, verse) ){
            return reference;
        }

        String chapterWord = TextUtil.findChapterWord(verse);
        
        if( StringUtils.contains(verse, chapterWord) ){
            return verse;
        }

        if( !StringUtils.contains(reference, chapterWord) ){
            return verse;
        }

        String chapter = "";
        if(StringUtils.contains(reference, chapterWord)){
            chapter = StringUtils.substring(reference, 0, StringUtils.indexOf(reference, chapterWord)+1);
        }

        return chapter + verse;
    }
    private String makeCompleteVerse(String book, String reference, String verse) {

        if( StringUtils.equals(reference, verse) ){
            return book + reference;
        }

        String chapterWord = TextUtil.returnChapterWord(book);

        if( StringUtils.contains(verse, chapterWord) ){
            return book + verse;
        }

        if( !StringUtils.contains(reference, chapterWord) ){
            return book + verse;
        }

        String chapter = "";
        if(StringUtils.contains(reference, chapterWord)){
            chapter = StringUtils.substring(reference, 0, StringUtils.indexOf(reference, chapterWord)+1);
        }

        return book + chapter + verse;
    }

    protected String processSentence(String content, boolean fixPronu) {
        if( fixPronu ){
            return replacePauseTag(phonetic.convert(replaceCHPunctuationWithBreakTag(abbr.convert(content))));
        }
        return replacePauseTag(replaceCHPunctuationWithBreakTag(abbr.convert(content)));
    }
}
