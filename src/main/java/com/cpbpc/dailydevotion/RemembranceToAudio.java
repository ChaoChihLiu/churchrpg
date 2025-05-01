package com.cpbpc.dailydevotion;

import com.cpbpc.comms.AWSUtil;
import com.cpbpc.comms.AbstractArticleParser;
import com.cpbpc.comms.AbstractComposer;
import com.cpbpc.comms.AppProperties;
import com.cpbpc.comms.Article;
import com.cpbpc.comms.ComposerResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import software.amazon.awssdk.services.s3.model.Tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static com.cpbpc.comms.AWSUtil.createS3Tag;

public class RemembranceToAudio {

    private static final Properties appProperties = AppProperties.getConfig();
    private Logger logger = Logger.getLogger(RemembranceToAudio.class.getName());

    public RemembranceToAudio() {
    }

    //    @Override
//    public Boolean handleRequest( Object input, Context context) {
    public Boolean handleRequest(Article convertData) {

        if (StringUtils.isEmpty(convertData.getContent())) {
            logger.info("No records found");
            return false;
        }
        logger.info("original title : " + convertData.getTitle());
        logger.info("original content : " + convertData.getContent());

        String month = convertData.getStartDate().split(" ")[0];
        String date =  convertData.getStartDate().split(" ")[1];
        AWSUtil.emptyTargetFolder(month, date, convertData.getTiming());

        AbstractComposer composer = initComposer(convertData);
        List<ComposerResult> results = composer.toTTS(true, convertData.getStartDate());

        if (Boolean.valueOf((String) AppProperties.getConfig().getOrDefault("use.polly", "false")) != true) {

            for( ComposerResult result : results ){
                System.out.println(result.getScript());
            }

            return true;
        }

        try {
            logger.info( "wait all audios ready" );
            waitAllPassageAudio(results);
            logger.info( "all audios are ready to merge" );
            merge(convertData, results);

        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.info(ExceptionUtils.getStackTrace(e));
        }
        
        return true;
    }

    private void waitUntilAudioMerged(List<Tag> tags) {
        String bucketName = "";
        String prefix = "";
        String objectKey = "";
        for( Tag tag : tags ){
            if(software.amazon.awssdk.utils.StringUtils.equals(tag.key(), "output_bucket") ){
                bucketName = tag.value();
            }
            if(software.amazon.awssdk.utils.StringUtils.equals(tag.key(), "output_prefix") ){
                prefix = tag.value();
            }
            if(software.amazon.awssdk.utils.StringUtils.equals(tag.key(), "audio_key") ){
                objectKey = tag.value();
            }
        }

        try {
            AWSUtil.waitUntilObjectReady(bucketName, prefix, objectKey, new Date());
        } catch (InterruptedException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }

    }
    
    private List<Tag> merge(Article article, List<ComposerResult> results) {

        List<String> fileNames = new ArrayList<>();
        for( ComposerResult result: results ){
            fileNames.add(result.getFileName());
        }

        List<Tag> mergeTags = new ArrayList<>();
        mergeTags.add(createS3Tag("output_bucket", appProperties.getProperty("output_bucket")));
        mergeTags.add(createS3Tag("output_prefix", appProperties.getProperty("output_prefix")));
        mergeTags.add(createS3Tag("output_format", appProperties.getProperty("output_format")));
        mergeTags.add(createS3Tag("audio_merged_bucket", appProperties.getProperty("audio_merged_bucket")));
        mergeTags.add(createS3Tag("audio_merged_prefix", appProperties.getProperty("audio_merged_prefix")));
        mergeTags.add(createS3Tag("audio_merged_format", appProperties.getProperty("audio_merged_format")));
        mergeTags.add(createS3Tag("name_prefix", appProperties.getProperty("name_prefix")));
        mergeTags.add(createS3Tag("publish_date", article.getStartDate()));
        mergeTags.add(createS3Tag("timing", article.getTiming()));

        String nameToBe = AppProperties.getConfig().getProperty("name_prefix") + "_" + article.getStartDate().replaceAll(" ", "_")+"_" + article.getTiming();
        String audioKey = appProperties.getProperty("audio_merged_prefix")+article.getStartDate().split(" ")[0]+"/"+nameToBe+"."+appProperties.getProperty("audio_merged_format");
        mergeTags.add(createS3Tag("audio_key", audioKey));

        AWSUtil.uploadS3Object( appProperties.getProperty("script_bucket"),
                appProperties.getProperty("script_prefix")+article.getStartDate().split(" ")[0]+"/"+article.getStartDate().split(" ")[1],
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
            if( StringUtils.equals("audio_key", tag.key()) ){
                return tag.value();
            }
        }

        return "";
    }
    
    private AbstractComposer initComposer(Article article) {
        AbstractArticleParser parser = initParser(article);
        return new Composer(parser);
    }

    private AbstractArticleParser initParser(Article article) {
        return new ArticleParser(article);

    }

}