package com.cpbpc.rpgv2;

import com.amazonaws.services.s3.model.Tag;
import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AppProperties;
import org.apache.commons.lang3.exception.ExceptionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
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
    public Boolean handleRequest(Article convertData) throws IOException {

        if (null == convertData.getContent()) {
            logger.info("No records found");
            return false;
        }
        logger.info("original : " + convertData.getContent());

        AbstractComposer composer = initComposer(appProperties.getProperty("language"), convertData.getContent(), convertData.getTitle());
        String content_modified = composer.toPolly(true);

        logger.info("content : " + content_modified);

        if (AppProperties.getTotalLength() + content_modified.length() + previousUsage >= pollyLimit) {
            logger.info("reached Polly Limit");
            return false;
        }

        AppProperties.addTotalLength(content_modified.length());
        logger.info(" total length " + AppProperties.getTotalLength());

        logger.info("use.polly is " + Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "true")));
        if (Boolean.valueOf((String) appProperties.getOrDefault("use.polly", "false")) == true) {
            logger.info("send to polly script S3 bucket!");
            List<Tag> tags = AWSUtil.putScriptToS3(content_modified, convertData.getStartDate());
            if( AppProperties.isChinese() ){
                waitUntilObjectReady(tags);
                AWSUtil.putPLScriptToS3(composer.toPolly(false), convertData.getStartDate());
            }
        }

        return true;
    }

    private void waitUntilObjectReady(List<Tag> tags) {
        String bucketName = "";
        String prefix = "";
        String objectKey = "";
        for( Tag tag : tags ){
            if(StringUtils.equals(tag.getKey(), "output_bucket") ){
                bucketName = tag.getValue();
            }
            if(StringUtils.equals(tag.getKey(), "output_prefix") ){
                prefix = tag.getValue();
            }
            if(StringUtils.equals(tag.getKey(), "audio_key") ){
                objectKey = tag.getValue();
            }
        }

        try {
            AWSUtil.waitUntilObjectReady(bucketName, prefix, objectKey, new Date());
        } catch (InterruptedException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }

    }

    private AbstractComposer initComposer(String language, String content, String title) {
        String packageName = "com.cpbpc.rpgv2." + language;
        AbstractArticleParser parser = initParser(language, content, title);
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

    private AbstractArticleParser initParser(String language, String content, String title) {

        String packageName = "com.cpbpc.rpgv2." + language;
        try {
            Class<?> clazz = Class.forName(packageName + ".ArticleParser");
            Constructor<?> constructor = clazz.getConstructor(String.class, String.class);
            Object obj = constructor.newInstance(content, title);
            if( obj instanceof AbstractArticleParser ){
                return (AbstractArticleParser)obj;
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

}