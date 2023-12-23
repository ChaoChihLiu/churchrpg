package com.cpbpc.rpgv2.zh;

import com.amazonaws.services.s3.model.Tag;
import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.TextUtil;
import com.cpbpc.comms.ThreadStorage;
import com.cpbpc.rpgv2.AbstractArticleParser;
import com.cpbpc.rpgv2.AbstractComposer;
import com.cpbpc.rpgv2.ConfigObj;
import com.cpbpc.rpgv2.VerseIntf;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

import static com.cpbpc.comms.PunctuationTool.pause;

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
    protected String toPolly(boolean fixPronu, String publishDate) {

        Map<String, String> scripts = splitPolly(fixPronu);
        Set<Map.Entry<String, String>> entries = scripts.entrySet();
        Map<String, List<Tag>> tagMap = new HashMap<>();
        StringBuilder builder = new StringBuilder();
        List<String> list = new ArrayList<>();
        for( Map.Entry<String, String> entry : entries ){
            String fileName = entry.getKey();
            list.add(fileName);

            String script = entry.getValue();
            builder.append(script);
            tagMap.put(fileName, sendToPolly(fileName, wrapToPolly(prettyPrintln(script)), publishDate));
        }

        try {
            waitAllPassageAudio(tagMap);
            List<Tag> mergeTags = mergeRPG(publishDate, list);
            waitUntilAudioMerged(mergeTags);
            AWSUtil.putPLScriptToS3(prettyPrintln(generatePLScript()), publishDate);
        } catch (InterruptedException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
        
        return builder.toString();
    }

    private String generatePLScript() {
        Map<String, String> scripts = splitPolly(false);
        Set<Map.Entry<String, String>> entries = scripts.entrySet();
        StringBuilder builder = new StringBuilder();
        for( Map.Entry<String, String> entry : entries ){
            String script = entry.getValue();
            builder.append(script);
        }

        return builder.toString();
    }

    private List<Tag> mergeRPG(String publishDate, List<String> fileNames) {
        List<Tag> mergeTags = new ArrayList<>();
        mergeTags.add(new Tag("output_bucket", appProperties.getProperty("output_bucket")));
        mergeTags.add(new Tag("output_prefix", appProperties.getProperty("output_prefix")));
        mergeTags.add(new Tag("output_format", appProperties.getProperty("output_format")));
        mergeTags.add(new Tag("audio_merged_bucket", appProperties.getProperty("audio_merged_bucket")));
        mergeTags.add(new Tag("audio_merged_prefix", appProperties.getProperty("audio_merged_prefix")));
        mergeTags.add(new Tag("audio_merged_format", appProperties.getProperty("audio_merged_format")));
        mergeTags.add(new Tag("name_prefix", appProperties.getProperty("name_prefix")));
        mergeTags.add(new Tag("publish_date", publishDate));

        String nameToBe = AppProperties.getConfig().getProperty("name_prefix") + publishDate.replaceAll("-", "");
        mergeTags.add(new Tag("audio_key", appProperties.getProperty("audio_merged_prefix")+publishDate.split("-")[0]+"_"+publishDate.split("-")[1]+"/"+nameToBe+"."+appProperties.getProperty("audio_merged_format")));

        AWSUtil.uploadS3Object( appProperties.getProperty("script_bucket"),
                appProperties.getProperty("script_prefix")+publishDate.split("-")[0]+"_"+publishDate.split("-")[1]+"/"+publishDate.split("-")[2],
                nameToBe+".audioMerge",
                StringUtils.join(fileNames, ","),
                mergeTags
        );

        return mergeTags;
    }

    private void waitAllPassageAudio(Map<String, List<Tag>> tagMap) throws InterruptedException {

        Set<Map.Entry<String, List<Tag>>> entries = tagMap.entrySet();
        Date uploadTime = new Date(System.currentTimeMillis());
        for( Map.Entry<String, List<Tag>> entry : entries ){
            String fileName = entry.getKey();
            List<Tag> tags = entry.getValue();
            if( tags.isEmpty() ){
                continue;
            }
            AWSUtil.waitUntilObjectReady( appProperties.getProperty("output_bucket"),
                    appProperties.getProperty("output_prefix"),
                    findAudioKey( entry.getValue() ),
                    uploadTime);

        }//end of for loop

    }//end of waitAllPassageAudio

    private String findAudioKey(List<Tag> tags) {
         for( Tag tag : tags ){
            if( StringUtils.equals("audio_key", tag.getKey()) ){
                return tag.getValue();
            }
         }

        return "";
    }

    private Map<String, String> splitPolly(boolean fixPronu) {
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

    private List<Tag> sendToPolly(String fileName, String content, String publishDate){

        List<Tag> tags = new ArrayList<>();
        logger.info("use.polly is " + Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "true")));
        if (Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "false")) != true) {
            return tags;
        }

        logger.info("send to polly script S3 bucket!");
        tags.addAll( AWSUtil.putZhScriptToS3(fileName, content, publishDate) );
//        waitUntilAudioMerged(tags);
//        AWSUtil.putPLScriptToS3(toPolly(false, publishDate), publishDate);

        return tags;
    }

    private void waitUntilAudioMerged(List<Tag> tags) {
        String bucketName = "";
        String prefix = "";
        String objectKey = "";
        for( Tag tag : tags ){
            if(software.amazon.awssdk.utils.StringUtils.equals(tag.getKey(), "output_bucket") ){
                bucketName = tag.getValue();
            }
            if(software.amazon.awssdk.utils.StringUtils.equals(tag.getKey(), "output_prefix") ){
                prefix = tag.getValue();
            }
            if(software.amazon.awssdk.utils.StringUtils.equals(tag.getKey(), "audio_key") ){
                objectKey = tag.getValue();
            }
        }

        try {
            AWSUtil.waitUntilObjectReady(bucketName, prefix, objectKey, new Date());
        } catch (InterruptedException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }

    }
}
