package com.cpbpc.rpgv2;

import com.cpbpc.comms.AppProperties;

import java.io.IOException;
import java.lang.reflect.Constructor;
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
        String content_modified = composer.toPolly(true, convertData.getStartDate());

        if (AppProperties.getTotalLength() + content_modified.length() + previousUsage >= pollyLimit) {
            logger.info("reached Polly Limit");
            return false;
        }

        AppProperties.addTotalLength(content_modified.length());
        logger.info(" total length " + AppProperties.getTotalLength());
        
        return true;
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