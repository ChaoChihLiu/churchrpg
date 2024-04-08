package com.cpbpc.rpgv2;

import com.amazonaws.services.s3.model.Tag;
import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AbstractArticleParser;
import com.cpbpc.comms.AbstractComposer;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.Article;
import com.cpbpc.comms.ComposerResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class RPGToAudio {

    private static final Properties appProperties = AppProperties.getConfig();
    private long previousUsage = 0;
    private long pollyLimit = 0;
    private Logger logger = Logger.getLogger(RPGToAudio.class.getName());

    public RPGToAudio(long previousUsage, long pollyLimit) {
        this.previousUsage = previousUsage;
        this.pollyLimit = pollyLimit;
    }

    //    @Override
//    public Boolean handleRequest( Object input, Context context) {
    public Boolean handleRequest(Article convertData) {

        if (StringUtils.isEmpty(convertData.getContent())) {
            logger.info("No records found");
            return false;
        }
        logger.info("original : " + convertData.getContent());

        String publishMonth = convertData.getStartDate().split("-")[0] + "_" + convertData.getStartDate().split("-")[1];
        String publishDate =  convertData.getStartDate().split("-")[2];
        AWSUtil.emptyTargetFolder(publishMonth, publishDate);

        AbstractComposer composer = initComposer(appProperties.getProperty("language"), convertData);
        List<ComposerResult> results = composer.toTTS(true, convertData.getStartDate());
        updatePollyCount( results );

        if (Boolean.valueOf((String) AppProperties.getConfig().getOrDefault("use.polly", "false")) != true) {
            return true;
        }
        
        try {
            logger.info( "wait all audios ready" );
            waitAllPassageAudio(results);
            logger.info( "all audios are ready to merge" );
            List<Tag> mergeTags = mergeRPG(convertData.getStartDate(), results);
            if( AppProperties.isChinese() ){
                logger.info( "wait for merged audio" );
                waitUntilAudioMerged(mergeTags);
                logger.info( "merged audio is done" );
//                logger.info( "merged audio is done and ready for PL" );

//                String month_str = convertData.getStartDate().split("-")[0] + "_" + convertData.getStartDate().split("-")[1];
//                String date_str = convertData.getStartDate().split("-")[2];
//
//                AWSUtil.putPLScriptToS3(composer.generatePLScript(), month_str, date_str);
//                logger.info( "PL is uploaded" );
            }
        } catch (InterruptedException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
        
        return true;
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

    private void updatePollyCount(List<ComposerResult> results) {
        StringBuilder builder = new StringBuilder();
        for( ComposerResult result: results ){
            builder.append(result.getScript());
        }

        if (AppProperties.getTotalLength() + builder.length() + previousUsage >= pollyLimit) {
            logger.info("reached Polly Limit");
            return;
        }

        AppProperties.addTotalLength(builder.length());
        logger.info(" total length " + AppProperties.getTotalLength());
    }
    
    private List<Tag> mergeRPG(String publishDate, List<ComposerResult> results) {

        List<String> fileNames = new ArrayList<>();
        for( ComposerResult result: results ){
            fileNames.add(result.getFileName());
        }

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

    private void waitAllPassageAudio(List<ComposerResult> results) throws InterruptedException {

        Date uploadTime = new Date(System.currentTimeMillis());
        for( ComposerResult result : results ){
            List<Tag> tags = result.getTags();
            if( tags.isEmpty() ){
                continue;
            }
            AWSUtil.waitUntilObjectReady( appProperties.getProperty("output_bucket"),
                    appProperties.getProperty("output_prefix"),
                    findAudioKey( tags ),
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
    
    private AbstractComposer initComposer(String language, Article article) {
        String packageName = "com.cpbpc.rpgv2." + language;
        AbstractArticleParser parser = initParser(language, article);
        try {
            Class<?> clazz = Class.forName(packageName + ".Composer");
            Constructor<?> constructor = clazz.getConstructor(AbstractArticleParser.class);
            Object obj = constructor.newInstance(parser);
            if( obj instanceof AbstractComposer ){
                return (AbstractComposer)obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private AbstractArticleParser initParser(String language, Article article) {

        String packageName = "com.cpbpc.rpgv2." + language;
        try {
            Class<?> clazz = Class.forName(packageName + ".ArticleParser");
            Constructor<?> constructor = clazz.getConstructor(Article.class);
            Object obj = constructor.newInstance(article);
            if( obj instanceof AbstractArticleParser){
                return (AbstractArticleParser)obj;
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}