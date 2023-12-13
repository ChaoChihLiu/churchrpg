package com.cpbpc.comms;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ObjectTagging;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.Tag;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringInputStream;
import com.amazonaws.util.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AWSUtil {

    private static java.util.logging.Logger logger = Logger.getLogger(AWSUtil.class.getName());

    private static AmazonS3 s3Client = null;
    static{
        if( s3Client == null ){
            s3Client = AmazonS3Client.builder().withRegion(AppProperties.getConfig().getProperty("region"))
                    .withCredentials(new DefaultAWSCredentialsProviderChain())
                    .build();
        }
    }
    
    private static void saveToS3(String content, String bucketName, String objectKey, String audioKey) {
        saveToS3(content, bucketName, objectKey, "", audioKey);
    }

    private static void saveToS3(String content, String bucketName, String objectKey, String publishDate_str, String audioKey) {
        try {
            InputStream inputStream = new StringInputStream(content);
            // Upload the file to S3
            ObjectMetadata metadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectKey, inputStream, metadata);

            List<Tag> tags = new ArrayList<>();
            if( !StringUtils.isNullOrEmpty(publishDate_str) ){
                tags.add(new Tag("publish_date", publishDate_str));
            }
            tags.add(new Tag("voice_id", AppProperties.getConfig().getProperty("voice_id")));
            tags.add(new Tag("category", URLDecoder.decode(AppProperties.getConfig().getProperty("content_category"))));
            tags.add(new Tag("audio_key", audioKey));
//            if( AppProperties.getConfig().containsKey("name_prefix") ){
//                tags.add(new Tag("name_prefix", AppProperties.getConfig().getProperty("name_prefix")));
//            }
            tags.add(new Tag("output_bucket", AppProperties.getConfig().getProperty("output_bucket")));
            tags.add(new Tag("output_format", AppProperties.getConfig().getProperty("output_format")));
            tags.add(new Tag("output_prefix", AppProperties.getConfig().getProperty("output_prefix")));
            tags.add(new Tag("engine", AppProperties.getConfig().getProperty("engine")));

            if( AppProperties.getConfig().containsKey("pl_script_bucket") ){
                tags.add(new Tag("pl_script_bucket", AppProperties.getConfig().getProperty("pl_script_bucket")));
                tags.add(new Tag("pl_script", StringUtils.replace(objectKey,
                                                                            AppProperties.getConfig().getProperty("script_format"),
                                                                            AppProperties.getConfig().getProperty("pl_format"))));
            }
            
            putObjectRequest.setStorageClass(StorageClass.IntelligentTiering);
            putObjectRequest.setTagging(new ObjectTagging(tags));

            s3Client.putObject(putObjectRequest);
        } catch (IOException e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
    }

    public static void putBibleScriptToS3(String content, String book, String chapter, String verseNum) throws IOException {

        book = StringUtils.replace(book, " ", "");
        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
        String prefix = AppProperties.getConfig().getProperty("script_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        String nameToBe = chapter+"/"+verseNum;
        String objectType = AppProperties.getConfig().getProperty("script_format");
        String objectKey = prefix + book + "/" + nameToBe + "." + objectType;
        String audioKey =  AppProperties.getConfig().getProperty("output_prefix") + book + "/" + nameToBe + "."
                + AppProperties.getConfig().getProperty("output_format");

        saveToS3(content, bucketName, objectKey, audioKey);

    }

    public static void putBiblePLScriptToS3(String content, String objectKey) {

        String bucketName = AppProperties.getConfig().getProperty("pl_script_bucket");
        String prefix = AppProperties.getConfig().getProperty("pl_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        String objectType = AppProperties.getConfig().getProperty("pl_format");
        objectKey = prefix + objectKey + "/" + "." + objectType;

        saveToS3(content, bucketName, objectKey, "");

    }

    public static void putPLScriptToS3(String content, String publishDate_str) {

        String bucketName = AppProperties.getConfig().getProperty("pl_script_bucket");
        String prefix = AppProperties.getConfig().getProperty("pl_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        String publishMonth = publishDate_str.split("-")[0] + "_" + publishDate_str.split("-")[1];
        String nameToBe = AppProperties.getConfig().getProperty("name_prefix") + publishDate_str.replaceAll("-", "");
        String objectType = AppProperties.getConfig().getProperty("pl_format");
        String objectKey = prefix + publishMonth + "/" + nameToBe + "." + objectType;
        String audioKey =  AppProperties.getConfig().getProperty("output_prefix")
                + publishMonth + "/"
                + nameToBe + "."
                + AppProperties.getConfig().getProperty("output_format");

        saveToS3(content, bucketName, objectKey, publishDate_str, audioKey);

    }

    public static void putScriptToS3(String content, String publishDate_str) throws IOException {

        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
        String prefix = AppProperties.getConfig().getProperty("script_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        String publishMonth = publishDate_str.split("-")[0] + "_" + publishDate_str.split("-")[1];
        String nameToBe = AppProperties.getConfig().getProperty("name_prefix") + publishDate_str.replaceAll("-", "");
        String objectType = AppProperties.getConfig().getProperty("script_format");
        String objectKey = prefix + publishMonth + "/" + nameToBe + "." + objectType;
        String audioKey =  AppProperties.getConfig().getProperty("output_prefix")
                            + publishMonth + "/"
                            + nameToBe + "."
                            + AppProperties.getConfig().getProperty("output_format");

        saveToS3(content, bucketName, objectKey, publishDate_str, audioKey);

    }

    public static void putScriptToS3(String content, String publishDate_str, String timing) throws IOException {

        String bucketName = AppProperties.getConfig().getProperty("script_bucket");
        String prefix = AppProperties.getConfig().getProperty("script_prefix");
        if (!prefix.endsWith("/")) {
            prefix += "/";
        }

        String publishMonth = StringUtils.trim(publishDate_str.split(" ")[0]);
        String publishDate = StringUtils.trim(publishDate_str.split(" ")[1]);
        String nameToBe = publishDate.replaceAll(" ", "")+"_"+ StringUtils.lowerCase(timing);
        String objectType = AppProperties.getConfig().getProperty("script_format");
        String objectKey = prefix + publishMonth + "/" + nameToBe + "." + objectType;
        String audioKey =  AppProperties.getConfig().getProperty("output_prefix")
                + publishMonth + "/"
                + nameToBe + "."
                + AppProperties.getConfig().getProperty("output_format");

        saveToS3(content, bucketName, objectKey, publishDate_str, audioKey);
    }

    public static String toPolly(String content){
        return "<speak><prosody rate='" + AppProperties.getConfig().getProperty("speech_speed")
                + "' volume='" + AppProperties.getConfig().getProperty("speech_volume") + "'>"
                + content
                + "</prosody></speak>";
    }

    public static void copyS3Objects(String bucketName, String outputPrefix, String outputFormat, String local_destination) {

        List<S3ObjectSummary> objects = listS3Objects(bucketName, outputPrefix);
        for( S3ObjectSummary object: objects ){
            String fileName = object.getKey();
            System.out.println("object key " + fileName);
            if( !org.apache.commons.lang3.StringUtils.endsWith(fileName, outputFormat) ){
                continue;
            }

            downloadS3Object(bucketName, fileName, local_destination+fileName);
        }
        

    }

    public static void downloadS3Object(String bucketName, String objectKey, String localFilePath){

        try {
            File audioFile = new File(localFilePath);
            if( !audioFile.exists() ){
                audioFile.createNewFile();
            }

            S3Object s3Object = s3Client.getObject(bucketName, objectKey);

            FileOutputStream fos = new FileOutputStream(localFilePath);
            IOUtils.copy(s3Object.getObjectContent(), fos);
            System.out.println("Object downloaded successfully to: " + localFilePath);
        } catch (Exception e) {
            logger.info(ExceptionUtils.getStackTrace(e));
        }
    }

    public static List<S3ObjectSummary> listS3Objects(String bucketName, String prefix){
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request();
        listObjectsRequest.setBucketName(bucketName);
        listObjectsRequest.setPrefix(prefix);

        ListObjectsV2Result listObjectsResponse = s3Client.listObjectsV2(listObjectsRequest);
        List<S3ObjectSummary> objects = listObjectsResponse.getObjectSummaries();
        return objects;
    }
}
