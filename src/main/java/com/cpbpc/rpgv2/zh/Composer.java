package com.cpbpc.rpgv2.zh;

import com.cpbpc.comms.AbstractArticleParser;
import com.cpbpc.comms.AbstractComposer;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.ComposerResult;
import com.cpbpc.comms.ConfigObj;
import com.cpbpc.comms.RomanNumeral;
import com.cpbpc.comms.TextUtil;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.comms.VerseIntf;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static com.cpbpc.comms.PunctuationTool.pause;
import static com.cpbpc.comms.PunctuationTool.replacePauseTag;
import static com.cpbpc.comms.PunctuationTool.replacePunctuationWithBreakTag;
import static com.cpbpc.comms.TextUtil.removeZhWhitespace;

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
            public List<ComposerResult> toTTS(boolean fixPronu, String publishDate) {
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
                    composerResult.addTags(sendToTTS(fileName, wrapToAzure(prettyPrintln(script)), publishDate, parser.getArticle().getCounter()));
                }

                return result;
    }
    
    protected Map<String, String> splitPolly(boolean fixPronu) {
        Map<String, String> scripts = new LinkedHashMap<>();
        int scriptCounter = 1;

        StringBuilder result = new StringBuilder();
        result.append(parser.readDate())
                .append(findWeekDay(parser.getArticle().getStartDate()))
                .append(pause(200));
        result.append("今日灵修题目").append(pause(200))
                .append(processSentence(RomanNumeral.convert(parser.getTitle(), true), fixPronu)).append(pause(400))
        ;
        scripts.put(scriptCounter+"_start", result.toString());
        scriptCounter++;

        int count = 0;
        try {
            for (String ref : parser.readTopicVerses()) {
                List<String> refs = verse.analyseVerse(removeLineWhitespace(ref), parser.getTopicVersePattern());
                String book = refs.get(0);
                for( int i=1; i<refs.size(); i++ ){
                    result = new StringBuilder();
                    count++;
                    result.append("圣经经文第" + count + "段").append(pause(200))
                            .append(processSentence(verse.convert(makeCompleteVerse(ZhConverterUtil.toSimple(book), refs.get(1), refs.get(i)), parser.getTopicVersePattern()), fixPronu)).append(pause(400))
//                            .append(processSentence(BibleVerseGrab.grab(mapBookAbbre(book), makeCompleteVerse(refs.get(1), refs.get(i))), fixPronu))
                    ;
                    List<String> verseContents = grabAndSplitVerse(BibleVerseGrab.grab(mapBookAbbre(book), makeCompleteVerse(refs.get(1), refs.get(i))));
                    for( String verseContent : verseContents ){
                        if( StringUtils.isEmpty(verseContent) ){
                            continue;
                        }
                        String processedVerse = verseContent;
                        if( AppProperties.isEnglish() ){
                            processedVerse = processSentence(verseContent, fixPronu);
                        }
                        if(verseContents.indexOf(verseContent) == 0){
                            result.append(processedVerse);
                            scripts.put(scriptCounter+"_biblePassage_"+count+"_"+(verseContents.indexOf(verseContent)+1), result.toString());
                        }else{
                            scripts.put(scriptCounter+"_biblePassage_"+count+"_"+(verseContents.indexOf(verseContent)+1), processedVerse);
                        }
                        scriptCounter++;
                    }
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
                .append(processSentence(RomanNumeral.convert(parser.getTitle(), true), fixPronu)).append(pause(800))
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
        String input = removeZhWhitespace(content);
        if( fixPronu ){
            return replacePauseTag(phonetic.convert(replacePunctuationWithBreakTag(abbr.convert(input))));
        }
        return replacePauseTag(replacePunctuationWithBreakTag(abbr.convert(input)));
    }

    protected String findWeekDay(String startDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(startDate, formatter);
        DayOfWeek dayOfWeek = date.getDayOfWeek();

//        DateTimeFormatter chineseFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.CHINA);
        String dayOfWeekInChinese = dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale.CHINA);
        if( StringUtils.contains(dayOfWeekInChinese, "日") || StringUtils.contains(dayOfWeekInChinese, "天") ){
            return "主日";
        }

        return StringUtils.replace(dayOfWeekInChinese, "星期", "禮拜");
    }
}
